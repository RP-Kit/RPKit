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
import com.rpkit.characters.bukkit.species.RPKSpeciesName
import com.rpkit.characters.bukkit.species.RPKSpeciesService
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * Character set species command.
 * Sets character's species.
 */
class CharacterSetSpeciesCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(RacePrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.species")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetSpecies)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.characters.set.species"))
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
        val speciesBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            speciesBuilder.append(args[i]).append(" ")
        }
        speciesBuilder.append(args[args.size - 1])
        val speciesService = Services[RPKSpeciesService::class.java]
        if (speciesService == null) {
            sender.sendMessage(plugin.messages.noSpeciesService)
            return completedFuture(MissingServiceFailure(RPKSpeciesService::class.java))
        }
        val species = speciesService.getSpecies(RPKSpeciesName(speciesBuilder.toString()))
        if (species == null) {
            sender.sendMessage(plugin.messages.characterSetSpeciesInvalidSpecies)
            return completedFuture(IncorrectUsageFailure())
        }
        character.species = species
        return characterService.updateCharacter(character).thenApply { updatedCharacter ->
            sender.sendMessage(plugin.messages.characterSetSpeciesValid)
            updatedCharacter?.showCharacterCard(sender)
            CommandSuccess
        }
    }

    private inner class RacePrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return Services[RPKSpeciesService::class.java]?.getSpecies(RPKSpeciesName(input)) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) return SpeciesSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return SpeciesSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return SpeciesSetPrompt()
            val speciesService = Services[RPKSpeciesService::class.java] ?: return SpeciesSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return SpeciesSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return SpeciesSetPrompt()
            character.species = speciesService.getSpecies(RPKSpeciesName(input))!!
            characterService.updateCharacter(character)
            return SpeciesSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.characterSetSpeciesInvalidSpecies
        }

        override fun getPromptText(context: ConversationContext): String {
            val speciesService = Services[RPKSpeciesService::class.java] ?: return plugin.messages.noSpeciesService
            val raceListBuilder = StringBuilder()
            for (species in speciesService.species) {
                raceListBuilder.append(plugin.messages.speciesListItem.withParameters(species)).append('\n')
            }
            return plugin.messages.characterSetSpeciesPrompt + "\n" + raceListBuilder.toString()
        }

    }

    private inner class SpeciesSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable is Player) {
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
                val raceService = Services[RPKSpeciesService::class.java]
                if (raceService == null) {
                    conversable.sendMessage(plugin.messages.noSpeciesService)
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
            return plugin.messages.characterSetSpeciesValid
        }

    }

}
