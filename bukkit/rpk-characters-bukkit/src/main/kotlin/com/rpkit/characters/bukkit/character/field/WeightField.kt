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
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.UnitType.Companion.WEIGHT
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class WeightField(private val plugin: RPKCharactersBukkit) : SettableCharacterCardField, HideableCharacterCardField {
    override fun isHidden(character: RPKCharacter): CompletableFuture<Boolean> {
        return completedFuture(character.isWeightHidden)
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean): CompletableFuture<Void> {
        val characterService = Services[RPKCharacterService::class.java]
        character.isWeightHidden = hidden
        return characterService?.updateCharacter(character)?.thenApply { null } ?: completedFuture(null)
    }

    override fun set(character: RPKCharacter, value: String): CompletableFuture<CharacterCardFieldSetResult> {
        val profile = character.profile
        if (profile == null) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noProfileSelf))
        }
        val profileId = profile.id
        if (profileId == null) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noProfileSelf))
        }
        val unitService = Services[RPKUnitService::class.java]
        if (unitService == null) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noUnitService))
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noCharacterService))
        }
        return unitService.getPreferredUnit(profileId, WEIGHT).thenApply { preferredWeightUnit ->
            val parsedValue = preferredWeightUnit.parse(value)
                ?: return@thenApply CharacterCardFieldSetFailure(plugin.messages.characterSetWeightInvalidWeight)
            character.weight = parsedValue
            characterService.updateCharacter(character)
            return@thenApply CharacterCardFieldSetSuccess
        }
    }

    override val name = "weight"

    override fun get(character: RPKCharacter): CompletableFuture<String> {
        val weight = character.weight ?: return completedFuture("unset")
        val profile = character.profile ?: return completedFuture("unset")
        val profileId = profile.id ?: return completedFuture("unset")
        val unitService = Services[RPKUnitService::class.java] ?: return completedFuture("unset")
        return unitService.getPreferredUnit(profileId, WEIGHT).thenApply { preferredWeightUnit ->
            unitService.format(preferredWeightUnit.scaleFactor * weight, preferredWeightUnit)
        }
    }

    override fun get(character: RPKCharacter, viewer: RPKProfile): CompletableFuture<String> {
        val weight = character.weight ?: return completedFuture("unset")
        val profileId = viewer.id ?: return completedFuture("unset")
        val unitService = Services[RPKUnitService::class.java] ?: return completedFuture("unset")
        return unitService.getPreferredUnit(profileId, WEIGHT).thenApply { preferredWeightUnit ->
            unitService.format(preferredWeightUnit.scaleFactor * weight, preferredWeightUnit)
        }
    }
}