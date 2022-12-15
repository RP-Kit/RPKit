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

package com.rpkit.classes.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.CharacterCardFieldSetFailure
import com.rpkit.characters.bukkit.character.field.CharacterCardFieldSetResult
import com.rpkit.characters.bukkit.character.field.CharacterCardFieldSetSuccess
import com.rpkit.characters.bukkit.character.field.SettableCharacterCardField
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassName
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture


class ClassField(private val plugin: RPKClassesBukkit) : SettableCharacterCardField {

    override val name = "class"

    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return Services[RPKClassService::class.java]?.getClass(character)?.thenApply {
            it?.name?.value ?: "unset"
        } ?: completedFuture("unset")
    }

    override fun set(character: RPKCharacter, value: String): CompletableFuture<CharacterCardFieldSetResult> {
        val classService = Services[RPKClassService::class.java] ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noClassService))
        val `class` = classService.getClass(RPKClassName(value)) ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.classSetInvalidClass))
        return `class`.hasPrerequisites(character).thenApplyAsync { hasPreqrequisites ->
            if (!hasPreqrequisites) {
                return@thenApplyAsync CharacterCardFieldSetFailure(plugin.messages.classSetInvalidPrerequisites)
            }
            if (character.age >= `class`.maxAge || character.age < `class`.minAge) {
                return@thenApplyAsync CharacterCardFieldSetFailure(plugin.messages.classSetInvalidAge
                    .withParameters(`class`.maxAge, `class`.minAge))
            }

            return@thenApplyAsync classService.setClass(character, `class`)
                .thenApply { CharacterCardFieldSetSuccess }
                .join()
        }
    }

}