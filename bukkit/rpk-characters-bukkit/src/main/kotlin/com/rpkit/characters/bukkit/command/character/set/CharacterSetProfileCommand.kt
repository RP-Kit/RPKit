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
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.InvalidTargetProfileFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * Character set profile command.
 * Transfers a character to another profile.
 */
class CharacterSetProfileCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(ProfilePrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.profile")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetProfile)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.set.profile"))
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
        if (args.isEmpty()) {
            val bukkitPlayer = sender.toBukkitPlayer()
            if (bukkitPlayer != null) {
                conversationFactory.buildConversation(bukkitPlayer).begin()
            }
            return completedFuture(CommandSuccess)
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return completedFuture(MissingServiceFailure(RPKProfileService::class.java))
        }
        if (!args[0].contains("#")) {
            sender.sendMessage(plugin.messages.characterSetProfileInvalidNoDiscriminator)
            return completedFuture(IncorrectUsageFailure())
        }
        val (name, discriminatorString) = args[0].split("#")
        val discriminator = discriminatorString.toIntOrNull()
        if (discriminator == null) {
            sender.sendMessage(plugin.messages.characterSetProfileInvalidDiscriminator)
            return completedFuture(IncorrectUsageFailure())
        }
        return profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).thenApply { newProfile ->
            if (newProfile == null) {
                sender.sendMessage(plugin.messages.characterSetProfileInvalidProfile)
                return@thenApply InvalidTargetProfileFailure()
            }
            character.profile = newProfile
            characterService.updateCharacter(character).thenApply { updatedCharacter ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    characterService.setActiveCharacter(sender, null).thenRun {
                        sender.sendMessage(plugin.messages.characterSetProfileValid)
                        updatedCharacter?.showCharacterCard(sender)
                    }
                })
            }
            CommandSuccess
        }
    }

    private inner class ProfilePrompt : ValidatingPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetProfilePrompt
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) return END_OF_CONVERSATION
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return END_OF_CONVERSATION
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return END_OF_CONVERSATION
            val characterService = Services[RPKCharacterService::class.java] ?: return END_OF_CONVERSATION
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return END_OF_CONVERSATION
            val profileService = Services[RPKProfileService::class.java] ?: return END_OF_CONVERSATION
            val (name, discriminatorString) = input.split("#")
            val discriminator = discriminatorString.toIntOrNull() ?: return END_OF_CONVERSATION
            val newProfile = profileService.getPreloadedProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return END_OF_CONVERSATION
            character.profile = newProfile
            characterService.updateCharacter(character)
            characterService.setActiveCharacter(minecraftProfile, null)
            return ProfileSetPrompt()
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val conversable = context.forWhom as? Player ?: return false
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return false
            val characterService = Services[RPKCharacterService::class.java] ?: return false
            characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return false
            val profileService = Services[RPKProfileService::class.java] ?: return false
            if (!input.contains("#")) return false
            val (name, discriminatorString) = input.split("#")
            val discriminator = discriminatorString.toIntOrNull() ?: return false
            profileService.getPreloadedProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return false
            return true
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            val conversable = context.forWhom as? Player ?: return plugin.messages.notFromConsole
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return plugin.messages.noMinecraftProfileService
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
                    ?: return plugin.messages.noMinecraftProfile
            val characterService = Services[RPKCharacterService::class.java] ?: return plugin.messages.noCharacterService
            characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return plugin.messages.noCharacter
            val profileService = Services[RPKProfileService::class.java] ?: return plugin.messages.characterSetProfileInvalidProfile
            if (!invalidInput.contains("#")) return plugin.messages.characterSetProfileInvalidNoDiscriminator
            val (name, discriminatorString) = invalidInput.split("#")
            val discriminator = discriminatorString.toIntOrNull() ?: return plugin.messages.characterSetProfileInvalidDiscriminator
            profileService.getPreloadedProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return plugin.messages.characterSetProfileInvalidProfile
            return ""
        }

    }

    private inner class ProfileSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return Prompt.END_OF_CONVERSATION
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                conversable.sendMessage(plugin.messages.noMinecraftProfileService)
                return END_OF_CONVERSATION
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                conversable.sendMessage(plugin.messages.noCharacterService)
                return END_OF_CONVERSATION
            }
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(context.forWhom as Player)
            if (minecraftProfile != null) {
                characterService.getPreloadedActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetProfileValid
        }

    }
}
