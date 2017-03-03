package com.rpkit.experience.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.CharacterCardField
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider


class ExperienceField(private val plugin: RPKExperienceBukkit): CharacterCardField {

    override val name = "experience"

    override fun get(character: RPKCharacter): String {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        return "${(experienceProvider.getExperience(character) - experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character)))}/${experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character) + 1) - experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character))}"
    }

}