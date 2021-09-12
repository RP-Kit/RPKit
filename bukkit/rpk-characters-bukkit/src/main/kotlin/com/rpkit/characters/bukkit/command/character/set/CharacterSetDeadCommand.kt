/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Character set dead command.
 * Sets character's dead state.
 */
class CharacterSetDeadCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(DeadPrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(plugin.messages["not-from-console"])
                .addConversationAbandonedListener { event ->
                    if (!event.gracefulExit()) {
                        val conversable = event.context.forWhom
                        if (conversable is Player) {
                            conversable.sendMessage(plugin.messages["operation-cancelled"])
                        }
                    }
                }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.characters.command.character.set.dead")) {
            sender.sendMessage(plugin.messages["no-permission-character-set-dead"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        val dead = args[0].toBoolean()
        if (dead && sender.hasPermission("rpkit.characters.command.character.set.dead.yes") || !dead && sender.hasPermission("rpkit.characters.command.character.set.dead.no")) {
            character.isDead = dead
            characterService.updateCharacter(character).thenAccept { updatedCharacter ->
                sender.sendMessage(plugin.messages["character-set-dead-valid"])
                updatedCharacter?.showCharacterCard(minecraftProfile)
                plugin.server.scheduler.runTask(plugin, Runnable {
                    if (dead) {
                        sender.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000, 0))
                        sender.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 1000000, 255))
                    } else {
                        sender.removePotionEffect(PotionEffectType.BLINDNESS)
                        sender.removePotionEffect(PotionEffectType.SLOW)
                    }
                })
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-character-set-dead-" + if (dead) "yes" else "no"])
            return true
        }
        return true
    }

    private inner class DeadPrompt : BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) return DeadSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return DeadSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return DeadSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return DeadSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return DeadSetPrompt()
            if (input && conversable.hasPermission("rpkit.characters.command.character.set.dead.yes") || !input && conversable.hasPermission("rpkit.characters.command.character.set.dead.no")) {
                character.isDead = input
                characterService.updateCharacter(character)
                if (input) {
                    conversable.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000, 0))
                    conversable.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 1000000, 255))
                } else {
                    conversable.removePotionEffect(PotionEffectType.BLINDNESS)
                    conversable.removePotionEffect(PotionEffectType.SLOW)
                }
                return DeadSetPrompt()
            } else {
                return DeadNotSetNoPermissionPrompt(input)
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
