package com.rpkit.skills.bukkit.skills

import com.rpkit.skills.bukkit.RPKSkillsBukkit


class RPKSkillTypeProviderImpl(private val plugin: RPKSkillsBukkit): RPKSkillTypeProvider {

    private val skillTypes: MutableList<RPKSkillType> = plugin.config.getStringList("skill-types").map(::RPKSkillTypeImpl).toMutableList()

    override fun getSkillTypes(): List<RPKSkillType> {
        return skillTypes
    }

    override fun getSkillType(name: String): RPKSkillType? {
        return skillTypes.firstOrNull { it.name == name }
    }

    override fun addSkillType(skillType: RPKSkillType) {
        skillTypes.add(skillType)
        plugin.config.set("skill-types", skillTypes.map(RPKSkillType::name))
        plugin.saveConfig()
    }

    override fun removeSkillType(skillType: RPKSkillType) {
        skillTypes.remove(skillType)
        plugin.config.set("skill-types", skillTypes.map(RPKSkillType::name))
        plugin.saveConfig()
    }

}