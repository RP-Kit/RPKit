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

package com.rpkit.characters.bukkit.character

import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

/**
 * Provides character-related operations.
 */
interface RPKCharacterService : Service {

    /**
     * Gets a character by ID.
     * If there is no character with the given ID, null is returned.
     *
     * @param id the ID of the character
     * @return The character, or null if no character is found with the given ID
     */
    fun getCharacter(id: Int): RPKCharacter?

    /**
     * Gets a Minecraft profile's active character.
     * If the profile does not currently have an active character, null is returned.
     *
     * @param minecraftProfile The Minecraft profile
     * @return The Minecraft profile's active character or null if the profile does not currently have an active character.
     */
    fun getActiveCharacter(minecraftProfile: RPKMinecraftProfile): RPKCharacter?

    /**
     * Sets a Minecraft profile's active character.
     *
     * @param minecraftProfile The Minecraft profile
     * @param character The character to set. May be null if the profile should be set to have no active character,
     *                  for example if they are moderating without a character
     */
    fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: RPKCharacter?)

    /**
     * Gets all characters currently playable by the owner of the given profile.
     *
     * @param profile The profile
     * @return All characters currently playable by the owner of the given profile.
     */
    fun getCharacters(profile: RPKProfile): Collection<RPKCharacter>

    /**
     * Gets all characters with a name similar to the given name.
     *
     * @param name The name
     * @return All characters with a name similar to the given name.
     */
    fun getCharacters(name: String): List<RPKCharacter>

    /**
     * Adds a character to be tracked by this character service.
     *
     * @param character The character to add
     */
    fun addCharacter(character: RPKCharacter)

    /**
     * Creates a new character. Any parameters not specified are defaulted.
     *
     * @param profile The profile
     * @param name The name
     * @param gender The gender
     * @param age The age
     * @param race The race
     * @param description The description
     * @param isDead Whether the character is dead
     * @param location The location
     * @param inventoryContents The inventory contents
     * @param helmet The helmet
     * @param chestplate The chestplate
     * @param leggings The leggings
     * @param boots The boots
     * @param health The health
     * @param maxHealth The max health
     * @param mana The mana
     * @param maxMana The max mana
     * @param foodLevel The food level
     * @param thirstLevel The thirst level
     * @param isProfileHidden Whether the profile is hidden
     * @param isNameHidden Whether the name is hidden
     * @param isGenderHidden Whether the gender is hidden
     * @param isAgeHidden Whether the age is hidden
     * @param isRaceHidden Whether the race is hidden
     * @param isDescriptionHidden Whether the description is hidden
     */
    fun createCharacter(
        profile: RPKProfile? = null,
        name: String? = null,
        gender: String? = null,
        age: Int? = null,
        race: RPKRace? = null,
        description: String? = null,
        isDead: Boolean? = null,
        location: Location? = null,
        inventoryContents: Array<ItemStack>? = null,
        helmet: ItemStack? = null,
        chestplate: ItemStack? = null,
        leggings: ItemStack? = null,
        boots: ItemStack? = null,
        health: Double? = null,
        maxHealth: Double? = null,
        mana: Int? = null,
        maxMana: Int? = null,
        foodLevel: Int? = null,
        thirstLevel: Int? = null,
        isProfileHidden: Boolean? = null,
        isNameHidden: Boolean? = null,
        isGenderHidden: Boolean? = null,
        isAgeHidden: Boolean? = null,
        isRaceHidden: Boolean? = null,
        isDescriptionHidden: Boolean? = null
    ): RPKCharacter

    /**
     * Removes a character from being tracked by this character service.
     *
     * @param character The character to remove
     */
    fun removeCharacter(character: RPKCharacter)

    /**
     * Updates a character's state in data storage.
     *
     * @param character The character to update
     */
    fun updateCharacter(character: RPKCharacter)

}