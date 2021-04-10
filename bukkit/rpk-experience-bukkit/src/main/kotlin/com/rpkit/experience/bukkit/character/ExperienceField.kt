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


class ExperienceField(private val plugin: RPKExperienceBukkit) : CharacterCardField {

    override val name = "experience"

    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            val experienceService =
                Services[RPKExperienceService::class.java] ?: return@supplyAsync ""
            val characterExperience = experienceService.getExperience(character).join()
            val characterLevel = experienceService.getLevel(character).join()
            val currentExperience =
                (characterExperience - experienceService.getExperienceNeededForLevel(characterLevel))
            val nextLevelExperience =
                experienceService.getExperienceNeededForLevel(characterLevel + 1) -
                        experienceService.getExperienceNeededForLevel(characterLevel)
            return@supplyAsync "$currentExperience/$nextLevelExperience"
        }
    }

}