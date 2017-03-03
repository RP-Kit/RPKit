package com.rpkit.experience.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.CharacterCardField
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider


class LevelField(private val plugin: RPKExperienceBukkit): CharacterCardField {

    override val name = "level"

    override fun get(character: RPKCharacter): String {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        return experienceProvider.getLevel(character).toString()
    }

}