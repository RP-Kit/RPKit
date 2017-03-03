package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider


interface RPKSkillPointProvider: ServiceProvider {

    fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int
    fun setSkillPoints(character: RPKCharacter, skillType: RPKSkillType, points: Int)

}