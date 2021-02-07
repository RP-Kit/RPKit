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

package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service

/**
 * Provides experience related operations.
 */
interface RPKExperienceService : Service {

    /**
     * Gets the level of a character.
     *
     * @param character The character
     * @return The level
     */
    fun getLevel(character: RPKCharacter): Int

    /**
     * Sets the level of a character.
     *
     * @param character The character
     * @return The level
     */
    fun setLevel(character: RPKCharacter, level: Int)

    /**
     * Gets the total experience earned of a character since its creation.
     *
     * @param character The character
     * @return The total experience earned by the character since its creation
     */
    fun getExperience(character: RPKCharacter): Int

    /**
     * Sets a character's total experience.
     *
     * @param character The character
     * @param experience The amount of experience to set
     */
    fun setExperience(character: RPKCharacter, experience: Int)

    /**
     * Gets the amount of experience required to reach a level.
     *
     * @param level The level
     */
    fun getExperienceNeededForLevel(level: Int): Int

}