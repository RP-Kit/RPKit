/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.experience.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.CharacterCardField
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import java.util.concurrent.CompletableFuture


class LevelField(private val plugin: RPKExperienceBukkit) : CharacterCardField {

    override val name = "level"

    override fun get(character: RPKCharacter): CompletableFuture<String> {
        val experienceService = Services[RPKExperienceService::class.java]
        if (experienceService == null) return CompletableFuture.completedFuture(plugin.messages["no-experience-service"])
        return CompletableFuture.completedFuture(experienceService.getLevel(character).toString())
    }

}