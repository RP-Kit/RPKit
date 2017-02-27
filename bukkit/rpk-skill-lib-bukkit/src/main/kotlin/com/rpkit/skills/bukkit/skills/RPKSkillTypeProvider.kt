package com.rpkit.skills.bukkit.skills

import com.rpkit.core.service.ServiceProvider


interface RPKSkillTypeProvider: ServiceProvider {

    fun getSkillTypes(): List<RPKSkillType>
    fun getSkillType(name: String): RPKSkillType?
    fun addSkillType(skillType: RPKSkillType)
    fun removeSkillType(skillType: RPKSkillType)

}