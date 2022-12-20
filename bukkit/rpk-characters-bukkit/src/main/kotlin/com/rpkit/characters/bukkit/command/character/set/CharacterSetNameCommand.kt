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
import com.rpkit.characters.bukkit.protocol.reloadPlayer
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
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
 * Character set name command.
 * Sets character's name.
 */
class CharacterSetNameCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(NamePrompt())
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
        if (!sender.hasPermission("rpkit.characters.command.character.set.name")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterSetName)
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.set.name"))
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
        val bukkitPlayer = sender.toBukkitPlayer()
        if (args.isEmpty()) {
            if (bukkitPlayer != null) {
                conversationFactory.buildConversation(bukkitPlayer).begin()
            }
            return completedFuture(CommandSuccess)
        }
        val nameBuilder = StringBuilder()
        for (i in 0 until args.size - 1) {
            nameBuilder.append(args[i]).append(" ")
        }
        nameBuilder.append(args[args.size - 1])
        character.name = nameBuilder.toString()
        return characterService.updateCharacter(character).thenApply { updatedCharacter ->
            if (plugin.config.getBoolean("characters.set-player-nameplate")
                && plugin.server.pluginManager.getPlugin("ProtocolLib") != null
                && bukkitPlayer != null) {
                reloadPlayer(bukkitPlayer, character, plugin.server.onlinePlayers.filter { it.uniqueId != sender.minecraftUUID })
            }
            sender.sendMessage(plugin.messages["character-set-name-valid"])
            updatedCharacter?.showCharacterCard(sender)
            return@thenApply CommandSuccess
        }
    }

    private inner class NamePrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.characterSetNamePrompt
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt {
            val conversable = context.forWhom
            if (conversable !is Player) return NameSetPrompt()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return NameSetPrompt()
            val characterService = Services[RPKCharacterService::class.java] ?: return NameSetPrompt()
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable) ?: return NameSetPrompt()
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return NameSetPrompt()
            if (input == null) return NameSetPrompt()
            character.name = input
            characterService.updateCharacter(character)
            if (plugin.config.getBoolean("characters.set-player-nameplate")
                && plugin.server.pluginManager.getPlugin("ProtocolLib") != null) {
                reloadPlayer(conversable, character, plugin.server.onlinePlayers.filter { it.uniqueId != conversable.uniqueId })
            }
            return NameSetPrompt()
        }

    }

    private inner class NameSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return END_OF_CONVERSATION
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
            return plugin.messages.characterSetNameValid
        }
    }
}
