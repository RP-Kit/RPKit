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

package com.rpkit.characters.bukkit.character.field

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.group.hasPermission
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import kotlin.math.max
import kotlin.math.min

/**
 * A character card field for age.
 */
class AgeField(private val plugin: RPKCharactersBukkit) : SettableCharacterCardField, HideableCharacterCardField {

    override val name = "age"
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return isHidden(character).thenApply { hidden ->
            if (hidden) {
                return@thenApply "[HIDDEN]"
            } else {
                return@thenApply character.age.toString()
            }
        }
    }

    override fun get(character: RPKCharacter, viewer: RPKProfile): CompletableFuture<String> {
        return isHidden(character).thenApplyAsync { hidden ->
            if (viewer.hasPermission("rpkit.characters.command.character.card.bypasshidden").join() || !hidden) {
                return@thenApplyAsync character.age.toString()
            } else {
                return@thenApplyAsync "[HIDDEN]"
            }
        }
    }

    override fun set(character: RPKCharacter, value: String): CompletableFuture<CharacterCardFieldSetResult> {
        val characterService = Services[RPKCharacterService::class.java]
            ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noCharacterService))
        val age = value.toIntOrNull() ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.characterSetAgeInvalidNumber))
        val minAge = max(plugin.config.getInt("characters.min-age"), character.species?.minAge ?: Int.MIN_VALUE)
        val maxAge = min(plugin.config.getInt("characters.max-age"), character.species?.maxAge ?: Int.MAX_VALUE)
        if (age !in minAge..maxAge) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.characterSetAgeInvalidValidation.withParameters(minAge, maxAge)))
        }
        character.age = age
        return characterService.updateCharacter(character).thenApply { CharacterCardFieldSetSuccess }
    }

    override fun isHidden(character: RPKCharacter): CompletableFuture<Boolean> {
        return completedFuture(character.isAgeHidden)
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean): CompletableFuture<Void> {
        character.isAgeHidden = hidden
        return Services[RPKCharacterService::class.java]?.updateCharacter(character)
            ?.thenApply { null }
            ?: completedFuture(null)
    }

}
