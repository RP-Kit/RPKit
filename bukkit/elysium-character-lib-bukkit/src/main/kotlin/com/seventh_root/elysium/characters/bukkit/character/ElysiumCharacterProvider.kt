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

package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Provides character-related operations.
 */
interface ElysiumCharacterProvider: ServiceProvider {

    /**
     * Gets a character by ID.
     * If there is no character with the given ID, null is returned.
     *
     * @param id the ID of the character
     * @return The character, or null if no character is found with the given ID
     */
    fun getCharacter(id: Int): ElysiumCharacter?

    /**
     * Gets a player's active character.
     * If the player does not currently have an active character, null is returned.
     *
     * @param player The player
     * @return The player's active character, or null if the player does not currently have an active character.
     */
    fun getActiveCharacter(player: ElysiumPlayer): ElysiumCharacter?

    /**
     * Sets a player's active character.
     *
     * @param player The player
     * @param character The character to set. May be null if the player should be set to have no active character,
     *                  for example if they are moderating without a character.
     */
    fun setActiveCharacter(player: ElysiumPlayer, character: ElysiumCharacter?)

    /**
     * Gets all characters currently playable by the given player.
     *
     * @param player The player
     * @return All characters currently playable by the player.
     */
    fun getCharacters(player: ElysiumPlayer): Collection<ElysiumCharacter>

    /**
     * Adds a character to be tracked by this character provider.
     *
     * @param character The character to add
     */
    fun addCharacter(character: ElysiumCharacter)

    /**
     * Removes a character from being tracked by this character provider.
     *
     * @param character The character to remove
     */
    fun removeCharacter(character: ElysiumCharacter)

    /**
     * Updates a character's state in data storage.
     *
     * @param character The character to update
     */
    fun updateCharacter(character: ElysiumCharacter)

}