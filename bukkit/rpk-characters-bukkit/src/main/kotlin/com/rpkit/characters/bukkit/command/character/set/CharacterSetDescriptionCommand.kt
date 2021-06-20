/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Character set description command.
 * Sets character's description state.
 */
class CharacterSetDescriptionCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(DescriptionPrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.description")) {
            sender.sendMessage(plugin.messages["no-permission-character-set-description"])
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
        val descriptionBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            descriptionBuilder.append(args[i]).append(" ")
        }
        descriptionBuilder.append(args[args.size - 1])
        character.description = descriptionBuilder.toString()
        characterService.updateCharacter(character).thenRun {
            sender.sendMessage(plugin.messages["character-set-description-valid"])
            character.showCharacterCard(minecraftProfile)
        }
        return true
    }

    private inner class DescriptionPrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-description-prompt"]
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt {
            if (context.getSessionData("description") == null) {
                context.setSessionData("description", "")
            }
            if (!input.equals("end", ignoreCase = true)) {
                val previousDescription = context.getSessionData("description") as String
                context.setSessionData("description", "$previousDescription $input")
                return DescriptionPrompt()
            }
            val conversable = context.forWhom
            if (conversable !is Player) return DescriptionSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return DescriptionSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return DescriptionSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
            if (minecraftProfile == null) return DescriptionSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
            if (character == null) return DescriptionSetPrompt()
            character.description = context.getSessionData("description") as String
            characterService.updateCharacter(character)
            return DescriptionSetPrompt()
        }

    }

    private inner class DescriptionSetPrompt : MessagePrompt() {

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
            return plugin.messages["character-set-description-valid"]
        }

    }
}
