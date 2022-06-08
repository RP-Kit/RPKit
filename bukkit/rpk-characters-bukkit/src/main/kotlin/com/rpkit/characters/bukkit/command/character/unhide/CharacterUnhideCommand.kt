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

package com.rpkit.characters.bukkit.command.character.unhide

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.character.field.HideableCharacterCardField
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.characters.bukkit.command.result.NoCharacterSelfFailure
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture

/**
 * Character unhide command.
 * Parent command for commands to unhide different character card fields.
 */
class CharacterUnhideCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.characterUnhideUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return CompletableFuture.completedFuture(NotAPlayerFailure())
        }
        val fieldName = args.joinToString(" ")
        val characterCardFieldService = Services[RPKCharacterCardFieldService::class.java]
        if (characterCardFieldService == null) {
            sender.sendMessage(plugin.messages.noCharacterCardFieldService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKCharacterCardFieldService::class.java))
        }
        val characterService=  Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKCharacterService::class.java))
        }
        val field = characterCardFieldService.characterCardFields
            .filterIsInstance<HideableCharacterCardField>()
            .firstOrNull { it.name == fieldName }
        if (field == null) {
            sender.sendMessage(plugin.messages.characterUnhideInvalidField
                .withParameters(
                    fields = characterCardFieldService.characterCardFields
                        .filterIsInstance<HideableCharacterCardField>()
                )
            )
            return CompletableFuture.completedFuture(InvalidFieldFailure())
        }
        if (!sender.hasPermission("rpkit.characters.command.character.unhide.${field.name}")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterUnhide.withParameters(field = field))
            return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.characters.command.character.unhide.${field.name}"))
        }
        val character = characterService.getPreloadedActiveCharacter(sender)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return CompletableFuture.completedFuture(NoCharacterSelfFailure())
        }
        return field.setHidden(character, false).thenApply {
            sender.sendMessage(plugin.messages.characterUnhideValid.withParameters(field = field))
            character.showCharacterCard(sender)
            return@thenApply CommandSuccess
        }
    }

    class InvalidFieldFailure : CommandFailure()

}
