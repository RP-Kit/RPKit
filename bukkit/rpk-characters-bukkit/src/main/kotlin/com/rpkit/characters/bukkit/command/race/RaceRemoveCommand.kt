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

package com.rpkit.characters.bukkit.command.race

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Race remove command.
 * Removes a race.
 */
class RaceRemoveCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(RacePrompt())
                .withEscapeSequence("cancel")
                .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(plugin.core.messages["operation-cancelled"])
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Conversable) {
            if (sender.hasPermission("rpkit.characters.command.race.remove")) {
                if (args.isNotEmpty()) {
                    val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
                    val raceBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        raceBuilder.append(args[i]).append(' ')
                    }
                    raceBuilder.append(args[args.size - 1])
                    val race = raceProvider.getRace(raceBuilder.toString())
                    if (race != null) {
                        raceProvider.removeRace(race)
                        sender.sendMessage(plugin.core.messages["race-remove-valid"])
                    } else {
                        sender.sendMessage(plugin.core.messages["race-remove-invalid-race"])
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(plugin.core.messages["no-permission-race-remove"])
            }
        }
        return true
    }

    private inner class RacePrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class).getRace(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
            raceProvider.removeRace(raceProvider.getRace(input)!!)
            return RaceSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.core.messages["race-remove-invalid-race"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["race-remove-prompt"]
        }

    }

    private inner class RaceSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.core.messages["race-remove-valid"]
        }

    }

}
