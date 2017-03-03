package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.skills.bukkit.skills.RPKSkillType


interface RPKClass {

    val name: String
    val maxLevel: Int
    fun hasPrerequisites(character: RPKCharacter): Boolean
    fun getSkillPoints(skillType: RPKSkillType, level: Int): Int

}