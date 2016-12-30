/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.characters.bukkit.gender

import com.rpkit.core.service.ServiceProvider

/**
 * Provides gender-related services.
 */
interface RPKGenderProvider: ServiceProvider {

    /**
     * A collection of genders currently managed by this gender provider.
     * This is immutable, genders must be added and removed using [addGender] and [removeGender].
     */
    val genders: Collection<RPKGender>

    /**
     * Gets a gender by ID.
     * If there is no gender with the given ID, null is returned.
     *
     * @param id The ID of the gender
     * @return The gender, or null if no gender is found with the given ID
     */
    fun getGender(id: Int): RPKGender?

    /**
     * Gets a gender by name.
     * If there is no gender with the given name, null is returned.
     *
     * @param name The name of the gender
     * @return The gender, or null if no gender is found with the given name
     */
    fun getGender(name: String): RPKGender?

    /**
     * Adds a gender to be tracked by this gender provider.
     *
     * @param gender The gender to add
     */
    fun addGender(gender: RPKGender)

    /**
     * Removes a gender from being tracked by this gender provider.
     *
     * @param gender The gender to remove
     */
    fun removeGender(gender: RPKGender)
}