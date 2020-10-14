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

package com.rpkit.stats.bukkit.stat

import com.rpkit.characters.bukkit.character.RPKCharacter

/**
 * Represents a stat.
 */
interface RPKStat {

    /**
     * The name of the stat.
     */
    val name: String

    /**
     * The expression used to calculate the stat.
     */
    val formula: String

    /**
     * Gets the stat for the given character, and the given stat variables.
     *
     * @param character The character
     * @param variables A list containing all variables required to get the stat.
     *                  Usually [RPKStatVariableService.statVariables]
     */
    fun get(character: RPKCharacter, variables: List<RPKStatVariable>): Int

}
