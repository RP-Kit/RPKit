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

package com.rpkit.characters.bukkit.race

import com.rpkit.characters.bukkit.species.RPKSpecies
import com.rpkit.characters.bukkit.species.RPKSpeciesName
import com.rpkit.core.service.Service

/**
 * Provides race-related services.
 */
interface RPKRaceService : Service {

    /**
     * A collection of races currently managed by this race service.
     * This is immutable.
     */
    @Deprecated("Use species", ReplaceWith("species"))
    val races: Collection<RPKRace>

    val species: Collection<RPKSpecies>
        get() = races

    /**
     * Gets a race by name.
     * If there is no race with the given name, null is returned.
     *
     * @param name The name of the race
     * @return The race, or null if no race is found with the given name
     */
    @Deprecated("Use getSpecies", ReplaceWith("getSpecies(name)"))
    fun getRace(name: RPKRaceName): RPKRace?

    /**
     * Gets a species by name.
     * If there is no species with the given name, null is returned.
     *
     * @param name The name of the species
     * @return The species, or null if no species is found with the given name
     */
    fun getSpecies(name: RPKSpeciesName): RPKSpecies? = getRace(name)

}