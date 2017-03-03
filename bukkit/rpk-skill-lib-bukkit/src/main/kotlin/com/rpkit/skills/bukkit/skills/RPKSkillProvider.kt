package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider


interface RPKSkillProvider: ServiceProvider {

    val skills: List<RPKSkill>
    fun getSkill(name: String): RPKSkill?
    fun addSkill(skill: RPKSkill)
    fun removeSkill(skill: RPKSkill)
    fun getSkillCooldown(character: RPKCharacter, skill: RPKSkill): Int
    fun setSkillCooldown(character: RPKCharacter, skill: RPKSkill, seconds: Int)

}