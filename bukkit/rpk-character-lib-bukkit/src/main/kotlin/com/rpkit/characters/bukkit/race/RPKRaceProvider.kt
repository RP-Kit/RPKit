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

package com.rpkit.characters.bukkit.race

import com.rpkit.core.service.ServiceProvider

/**
 * Provides race-related services.
 */
interface RPKRaceProvider: ServiceProvider {

    /**
     * A collection of races currently managed by this race provider.
     * This is immutable, races must be added and removed using [addRace] and [removeRace].
     */
    val races: Collection<RPKRace>

    /**
     * Gets a race by ID.
     * If there is no race with the given ID, null is returned.
     *
     * @param id The ID of the race
     * @return The race, or null if no race is found with the given ID
     */
    fun getRace(id: Int): RPKRace?

    /**
     * Gets a race by name.
     * If there is no race with the given name, null is returned.
     *
     * @param name The name of the race
     * @return The race, or null if no race is found with the given name
     */
    fun getRace(name: String): RPKRace?

    /**
     * Adds a race to be tracked by this race provider.
     *
     * @param race The race to add
     */
    fun addRace(race: RPKRace)

    /**
     * Removes a race from being tracked by this race provider.
     *
     * @param race The race to remove
     */
    fun removeRace(race: RPKRace)

}