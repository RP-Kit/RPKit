package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider

/**
 * Provides experience related operations.
 */
interface RPKExperienceProvider: ServiceProvider {

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