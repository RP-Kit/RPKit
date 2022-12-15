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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * A character card field for dead.
 */
class DeadField(private val plugin: RPKCharactersBukkit) : SettableCharacterCardField {

    override val name = "dead"
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return completedFuture(character.isDead.toString())
    }

    override fun set(character: RPKCharacter, value: String): CompletableFuture<CharacterCardFieldSetResult> {
        val characterService = Services[RPKCharacterService::class.java] ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noCharacterService))
        val isDead = value.toBooleanStrictOrNull() ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.characterSetDeadInvalidBoolean))
        character.isDead = isDead
        return characterService.updateCharacter(character).thenApply { CharacterCardFieldSetSuccess }
    }

}
