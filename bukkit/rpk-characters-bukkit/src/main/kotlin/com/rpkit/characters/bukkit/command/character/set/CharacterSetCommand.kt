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
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.character.field.CharacterCardFieldSetFailure
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.characters.bukkit.character.field.SettableCharacterCardField
import com.rpkit.characters.bukkit.command.result.NoCharacterSelfFailure
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * Character set command.
 * Parent command for commands used to set character attributes.
 */
class CharacterSetCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(ValuePrompt())
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

    private val characterSetProfileCommand = CharacterSetProfileCommand(plugin)
    private val characterSetNameCommand = CharacterSetNameCommand(plugin)
    private val characterSetGenderCommand = CharacterSetGenderCommand(plugin)
    private val characterSetAgeCommand = CharacterSetAgeCommand(plugin)
    private val characterSetSpeciesCommand = CharacterSetSpeciesCommand(plugin)
    private val characterSetDescriptionCommand = CharacterSetDescriptionCommand(plugin)
    private val characterSetDeadCommand = CharacterSetDeadCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return completedFuture(MissingServiceFailure(RPKCharacterService::class.java))
        }
        val characterCardFieldService = Services[RPKCharacterCardFieldService::class.java]
        if (characterCardFieldService == null) {
            sender.sendMessage(plugin.messages.noCharacterCardFieldService)
            return completedFuture(MissingServiceFailure(RPKCharacterCardFieldService::class.java))
        }
        return when  {
            args.firstOrNull()?.equals("profile", ignoreCase = true) == true ->
                characterSetProfileCommand.onCommand(sender, args.drop(1).toTypedArray())
            args.firstOrNull()?.equals("name", ignoreCase = true) == true ->
                characterSetNameCommand.onCommand(sender, args.drop(1).toTypedArray())
            args.firstOrNull()?.equals("gender", ignoreCase = true) == true ->
                characterSetGenderCommand.onCommand(sender, args.drop(1).toTypedArray())
            args.firstOrNull()?.equals("age", ignoreCase = true) == true ->
                characterSetAgeCommand.onCommand(sender, args.drop(1).toTypedArray())
            args.firstOrNull()?.equals("species", ignoreCase = true) == true || args.firstOrNull()?.equals("race", ignoreCase = true) == true ->
                characterSetSpeciesCommand.onCommand(sender, args.drop(1).toTypedArray())
            args.firstOrNull()?.equals("description", ignoreCase = true) == true || args.firstOrNull()?.equals("desc", ignoreCase = true) == true ->
                characterSetDescriptionCommand.onCommand(sender, args.drop(1).toTypedArray())
            args.firstOrNull()?.equals("dead", ignoreCase = true) == true ->
                characterSetDeadCommand.onCommand(sender, args.drop(1).toTypedArray())
            characterCardFieldService.characterCardFields
                .filterIsInstance<SettableCharacterCardField>()
                .any { field -> args.firstOrNull()?.equals(field.name, ignoreCase = true) == true } -> {
                if (sender !is RPKMinecraftProfile) {
                    sender.sendMessage(plugin.messages.notFromConsole)
                    return completedFuture(NotAPlayerFailure())
                }
                val character = characterService.getPreloadedActiveCharacter(sender)
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return completedFuture(NoCharacterSelfFailure())
                }
                val characterCardField = characterCardFieldService.characterCardFields
                    .filterIsInstance<SettableCharacterCardField>()
                    .firstOrNull { field -> args.first().equals(field.name, ignoreCase = true) }
                if (characterCardField == null) {
                    sender.sendMessage(plugin.messages.characterSetUsage)
                    return completedFuture(IncorrectUsageFailure())
                }
                if (args.size <= 1) {
                    val bukkitPlayer = sender.toBukkitPlayer()
                    if (bukkitPlayer != null) {
                        val conversation = conversationFactory.buildConversation(bukkitPlayer)
                        with (conversation.context) {
                            setSessionData("minecraftProfile", sender)
                            setSessionData("character", character)
                            setSessionData("field", characterCardField)
                        }
                        conversation.begin()
                        return completedFuture(CommandSuccess)
                    }
                }
                val value = args.drop(1).joinToString(" ")
                return setCharacterCardField(sender, character, characterCardField, value)
            }
            else -> {
                sender.sendMessage(plugin.messages.characterSetUsage)
                completedFuture(IncorrectUsageFailure())
            }
        }
    }

    private fun setCharacterCardField(
        minecraftProfile: RPKMinecraftProfile,
        character: RPKCharacter,
        characterCardField: SettableCharacterCardField,
        value: String
    ): CompletableFuture<CommandResult> {
        return characterCardField.set(character, value).thenApply { result ->
            if (result is CharacterCardFieldSetFailure) {
                minecraftProfile.sendMessage(result.message)
                return@thenApply CharacterSetCharacterCardFieldFailure(result)
            } else {
                minecraftProfile.sendMessage(
                    plugin.messages.characterSetValid
                        .withParameters(
                            field = characterCardField,
                            value = value
                        )
                )
                return@thenApply CommandSuccess
            }
        }
    }

    class CharacterSetCharacterCardFieldFailure(
        private val characterCardFieldSetFailure: CharacterCardFieldSetFailure
    ) : CommandFailure()

    private inner class ValuePrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            val field = context.getSessionData("field") as SettableCharacterCardField
            return plugin.messages.characterSetPrompt
                .withParameters(field = field)
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
            val minecraftProfile = context.getSessionData("minecraftProfile") as RPKMinecraftProfile
            val character = context.getSessionData("character") as RPKCharacter
            val field = context.getSessionData("field") as SettableCharacterCardField
            if (input == null) return END_OF_CONVERSATION
            setCharacterCardField(minecraftProfile, character, field, input)
            return END_OF_CONVERSATION
        }

    }

}
