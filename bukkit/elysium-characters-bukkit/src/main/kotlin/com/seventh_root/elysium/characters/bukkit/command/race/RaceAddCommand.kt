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

package com.seventh_root.elysium.characters.bukkit.command.race

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceImpl
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class RaceAddCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(RacePrompt()).withEscapeSequence("cancel").addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Conversable) {
            if (sender.hasPermission("elysium.characters.command.race.add")) {
                if (args.size > 0) {
                    val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class.java)
                    val raceBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        raceBuilder.append(args[i]).append(' ')
                    }
                    raceBuilder.append(args[args.size - 1])
                    if (raceProvider.getRace(raceBuilder.toString()) == null) {
                        raceProvider.addRace(ElysiumRaceImpl(name = raceBuilder.toString()))
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-add-valid")))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-add-")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-race-add")))
            }
        }
        return true
    }

    private inner class RacePrompt: ValidatingPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-add-prompt"))
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class.java)
            return raceProvider.getRace(input) == null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class.java)
            raceProvider.addRace(ElysiumRaceImpl(name = input))
            return RaceAddedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-add-invalid-race"))
        }

    }

    private inner class RaceAddedPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-add-valid"))
        }

    }

}
