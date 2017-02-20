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

package com.rpkit.characters.bukkit.command.gender

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Gender remove command.
 * Removes a gender.
 */
class GenderRemoveCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(GenderPrompt())
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
        if (sender is Conversable) {
            if (sender.hasPermission("rpkit.characters.command.gender.remove")) {
                if (args.isNotEmpty()) {
                    val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
                    val genderBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        genderBuilder.append(args[i]).append(' ')
                    }
                    genderBuilder.append(args[args.size - 1])
                    val gender = genderProvider.getGender(genderBuilder.toString())
                    if (gender != null) {
                        genderProvider.removeGender(gender)
                        sender.sendMessage(plugin.messages["gender-remove-valid"])
                    } else {
                        sender.sendMessage(plugin.messages["gender-remove-invalid-gender"])
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-gender-remove"])
            }
        }
        return true
    }

    private inner class GenderPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class).getGender(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
            genderProvider.removeGender(genderProvider.getGender(input)!!)
            return GenderSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return plugin.messages["gender-remove-invalid-gender"]
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["gender-remove-prompt"]
        }

    }

    private inner class GenderSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["gender-remove-valid"]
        }

    }

}
