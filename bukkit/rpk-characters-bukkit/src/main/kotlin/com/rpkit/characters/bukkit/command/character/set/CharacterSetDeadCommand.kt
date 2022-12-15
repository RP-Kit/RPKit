/*
 * Copyright 2022 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.characters.bukkit.command.character.set

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.command.result.NoCharacterSelfFailure
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * Character set dead command.
 * Sets character's dead state.
 */
class CharacterSetDeadCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(DeadPrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(plugin.messages.notFromConsole)
                .addConversationAbandonedListener { event ->
                    if (!event.gracefulExit()) {
                        val conversable = event.context.forWhom
                        if (conversable is Player) {
                            conversable.sendMessage(plugin.messages.operationCancelled)
                        }
                    }
                }
    }

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        if (!sender.hasPermission("rpkit.characters.command.character.set.dead")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetDead)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.set.dead"))
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return completedFuture(MissingServiceFailure(RPKCharacterService::class.java))
        }
        val character = characterService.getPreloadedActiveCharacter(sender)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return completedFuture(NoCharacterSelfFailure())
        }
        val bukkitPlayer = sender.toBukkitPlayer()
        if (args.isEmpty()) {

            if (bukkitPlayer != null) {
                conversationFactory.buildConversation(bukkitPlayer).begin()
            }
            return completedFuture(CommandSuccess)
        }
        val dead = args[0].toBoolean()
        if (dead && sender.hasPermission("rpkit.characters.command.character.set.dead.yes") || !dead && sender.hasPermission("rpkit.characters.command.character.set.dead.no")) {
            character.isDead = dead
            return characterService.updateCharacter(character).thenApply { updatedCharacter ->
                sender.sendMessage(plugin.messages.characterSetDeadValid)
                updatedCharacter?.showCharacterCard(sender)
                plugin.server.scheduler.runTask(plugin, Runnable {
                    if (dead) {
                        if (bukkitPlayer != null) {
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000, 0))
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 1000000, 255))
                        }
                    } else {
                        if (bukkitPlayer != null) {
                            bukkitPlayer.removePotionEffect(PotionEffectType.BLINDNESS)
                            bukkitPlayer.removePotionEffect(PotionEffectType.SLOW)
                        }
                    }
                })
                CommandSuccess
            }
        } else {
            sender.sendMessage(
                if (dead) {
                    plugin.messages.noPermissionCharacterSetDeadYes
                } else {
                    plugin.messages.noPermissionCharacterSetDeadNo
                }
            )
            return completedFuture(NoPermissionFailure(
                if (dead) {
                    "rpkit.characters.command.character.set.dead.yes"
                } else {
                    "rpkit.characters.command.character.set.dead.no"
                }
            ))
        }
    }

    private inner class DeadPrompt : BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) return DeadSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return DeadSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return DeadSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return DeadSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return DeadSetPrompt()
            return if (input && conversable.hasPermission("rpkit.characters.command.character.set.dead.yes") || !input && conversable.hasPermission("rpkit.characters.command.character.set.dead.no")) {
                character.isDead = input
                characterService.updateCharacter(character)
                if (input) {
                    conversable.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000, 0))
                    conversable.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 1000000, 255))
                } else {
                    conversable.removePotionEffect(PotionEffectType.BLINDNESS)
                    conversable.removePotionEffect(PotionEffectType.SLOW)
                }
                DeadSetPrompt()
            } else {
                DeadNotSetNoPermissionPrompt(input)
            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["character-set-dead-invalid-boolean"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-dead-prompt"]
        }

    }

    private inner class DeadSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    conversable.sendMessage(plugin.messages["no-minecraft-profile-service"])
                    return END_OF_CONVERSATION
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    conversable.sendMessage(plugin.messages["no-character-service"])
                    return END_OF_CONVERSATION
                }
                val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(context.forWhom as Player)
                if (minecraftProfile != null) {
                    characterService.getPreloadedActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
                }
            }
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-dead-valid"]
        }

    }

    private inner class DeadNotSetNoPermissionPrompt(private val dead: Boolean) : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    conversable.sendMessage(plugin.messages["no-minecraft-profile-service"])
                    return END_OF_CONVERSATION
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    conversable.sendMessage(plugin.messages["no-character-service"])
                    return END_OF_CONVERSATION
                }
                val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(context.forWhom as Player)
                if (minecraftProfile != null) {
                    characterService.getPreloadedActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
                }
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["no-permission-character-set-dead-" + if (dead) "yes" else "no"]
        }

    }
}
