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

package com.rpkit.characters.bukkit.command.character.hide

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
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * Character hide command.
 * Parent command for commands to hide different character card fields.
 */
class CharacterHideCommand(private val plugin: RPKCharactersBukkit) : RPKCommandExecutor {

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.characterHideUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val fieldName = args.joinToString(" ")
        val characterCardFieldService = Services[RPKCharacterCardFieldService::class.java]
        if (characterCardFieldService == null) {
            sender.sendMessage(plugin.messages.noCharacterCardFieldService)
            return completedFuture(MissingServiceFailure(RPKCharacterCardFieldService::class.java))
        }
        val characterService=  Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return completedFuture(MissingServiceFailure(RPKCharacterService::class.java))
        }
        val field = characterCardFieldService.characterCardFields
            .filterIsInstance<HideableCharacterCardField>()
            .firstOrNull { it.name == fieldName }
        if (field == null) {
            sender.sendMessage(plugin.messages.characterHideInvalidField
                .withParameters(
                    fields = characterCardFieldService.characterCardFields
                        .filterIsInstance<HideableCharacterCardField>()
                )
            )
            return completedFuture(InvalidFieldFailure())
        }
        if (!sender.hasPermission("rpkit.characters.command.character.hide.${field.name}")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterHide.withParameters(field = field))
            return completedFuture(NoPermissionFailure("rpkit.characters.command.character.hide.${field.name}"))
        }
        val character = characterService.getPreloadedActiveCharacter(sender)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return completedFuture(NoCharacterSelfFailure())
        }
        return field.setHidden(character, true).thenApply {
            sender.sendMessage(plugin.messages.characterHideValid.withParameters(field = field))
            character.showCharacterCard(sender)
            return@thenApply CommandSuccess
        }
    }

    class InvalidFieldFailure : CommandFailure()

}
