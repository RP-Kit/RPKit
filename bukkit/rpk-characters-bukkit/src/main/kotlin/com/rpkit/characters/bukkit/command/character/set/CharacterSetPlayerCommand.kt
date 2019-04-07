/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Character set player command.
 * Transfers a character to another player.
 */
class CharacterSetPlayerCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(PlayerPrompt(plugin))
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
        if (sender is Player) {
            if (sender.hasPermission("rpkit.characters.command.character.set.player")) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        if (args.isNotEmpty()) {
                            @Suppress("DEPRECATION") val newBukkitPlayer = plugin.server.getPlayer(args[0])
                            if (newBukkitPlayer != null) {
                                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                                val newPlayer = playerProvider.getPlayer(newBukkitPlayer)
                                character.player = newPlayer
                                characterProvider.updateCharacter(character)
                                characterProvider.setActiveCharacter(minecraftProfile, null)
                                sender.sendMessage(plugin.messages["character-set-player-valid"])
                                character.showCharacterCard(minecraftProfile)
                            } else {
                                sender.sendMessage(plugin.messages["character-set-player-invalid-player"])
                            }
                        } else {
                            conversationFactory.buildConversation(sender).begin()
                        }
                    } else {
                        sender.sendMessage(plugin.messages["no-character"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-character-set-player"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

    private inner class PlayerPrompt(plugin: Plugin): PlayerNamePrompt(plugin) {

        override fun acceptValidatedInput(context: ConversationContext, input: Player): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(conversable)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                        character.player = playerProvider.getPlayer(input)
                        characterProvider.updateCharacter(character)
                        characterProvider.setActiveCharacter(minecraftProfile, null)
                    }
                }
            }
            return PlayerSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["character-set-player-invalid-player"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-player-prompt"]
        }

    }

    private inner class PlayerSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(context.forWhom as Player)
                if (minecraftProfile != null) {
                    characterProvider.getActiveCharacter(minecraftProfile)?.showCharacterCard(minecraftProfile)
                }
            }
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["character-set-player-valid"]
        }

    }
}
