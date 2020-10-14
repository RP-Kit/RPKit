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

package com.rpkit.characters.bukkit.command.race

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Race remove command.
 * Removes a race.
 */
class RaceRemoveCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {
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
                            conversable.sendMessage(plugin.messages["operation-cancelled"])
                        }
                    }
                }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Conversable) return true
        if (!sender.hasPermission("rpkit.characters.command.race.remove")) {
            sender.sendMessage(plugin.messages["no-permission-race-remove"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        val raceService = Services[RPKRaceService::class]
        if (raceService == null) {
            sender.sendMessage(plugin.messages["no-race-service"])
            return true
        }
        val raceBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            raceBuilder.append(args[i]).append(' ')
        }
        raceBuilder.append(args[args.size - 1])
        val race = raceService.getRace(raceBuilder.toString())
        if (race == null) {
            sender.sendMessage(plugin.messages["race-remove-invalid-race"])
            return true
        } else {
            raceService.removeRace(race)
            sender.sendMessage(plugin.messages["race-remove-valid"])
        }
        return true
    }

    private inner class RacePrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return Services[RPKRaceService::class]?.getRace(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val raceService = Services[RPKRaceService::class] ?: return RaceSetPrompt()
            context.setSessionData("raceService", raceService)
            val race = raceService.getRace(input) ?: return RaceSetPrompt()
            context.setSessionData("race", race)
            raceService.removeRace(race)
            return RaceSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["race-remove-invalid-race"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["race-remove-prompt"]
        }

    }

    private inner class RaceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            if (context.getSessionData("raceService") == null) return plugin.messages["no-race-service"]
            if (context.getSessionData("race") == null) return plugin.messages["race-remove-invalid-race"]
            return plugin.messages["race-remove-valid"]
        }

    }

}
