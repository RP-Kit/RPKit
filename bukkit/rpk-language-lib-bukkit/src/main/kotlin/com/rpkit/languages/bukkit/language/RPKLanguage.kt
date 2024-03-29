/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.languages.bukkit.language

import com.rpkit.characters.bukkit.species.RPKSpecies

interface RPKLanguage {

    val name: RPKLanguageName

    /**
     * Gets the base understanding for a character of a given species of this language
     *
     * @param species The character's species
     * @return The base understanding of a new character of the given species
     */
    fun getBaseUnderstanding(species: RPKSpecies): Float

    /**
     * Gets the minimum understanding increment for a character of the given species
     *
     * @param species The character's species
     * @return The minimum increment of the character's understanding upon hearing a message of this language
     */
    fun getMinimumUnderstandingIncrement(species: RPKSpecies): Float

    /**
     * Gets the maximum understanding increment for a character of the given species
     *
     * @param species The character's species
     * @return The maximum increment of the character's understanding upon hearing a message of this language
     */
    fun getMaximumUnderstandingIncrement(species: RPKSpecies): Float

    /**
     * Generates a random understanding increment between the minimum and maximum understanding increments for
     * a character of the given species
     *
     * @param species The character's species
     * @return A random understanding increment between the minimum and maximum understanding increments for the species
     */
    fun randomUnderstandingIncrement(species: RPKSpecies): Float

    /**
     * Applies the language to a message, given a sender and a receiver's understanding of the language.
     * The sender's understanding should be used to determine how garbled the message comes out, and the receiver's
     * understanding should be used to determine how much of the time the cypher should be applied, though this may
     * vary by implementation.
     *
     * @param message The spoken message
     * @param senderUnderstanding The amount the sender understands the language. Minimum 0, maximum 100.
     * @param receiverUnderstanding The amount the receiver understands the language. Minimum 0, maximum 100.
     * @return The message with any cyphers/garble etc required applied.
     */
    fun apply(message: String, senderUnderstanding: Float, receiverUnderstanding: Float): String

}