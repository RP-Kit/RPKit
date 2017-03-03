package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.skills.bukkit.skills.RPKSkillType
import com.rpkit.skills.bukkit.skills.RPKSkillTypeProvider


class RPKClassImpl(
        private val plugin: RPKClassesBukkit,
        override val name: String,
        override val maxLevel: Int,
        private val prerequisitesByName: Map<String, Int>,
        private val baseSkillPointsByName: Map<String, Int>,
        private val levelSkillPointsByName: Map<String, Int>
) : RPKClass {

    val prerequisites: Map<RPKClass, Int>
        get() = prerequisitesByName
                .map { entry ->
                    Pair(
                            plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class).getClass(entry.key)!!,
                            entry.value
                    )
                }
                .toMap()

    val baseSkillPoints: Map<RPKSkillType, Int>
        get() = baseSkillPointsByName
                .map { entry ->
                    Pair(
                            plugin.core.serviceManager.getServiceProvider(RPKSkillTypeProvider::class).getSkillType(entry.key)!!,
                            entry.value
                    )
                }
                .toMap()

    val levelSkillPoints: Map<RPKSkillType, Int>
        get() = levelSkillPointsByName
                .map { entry ->
                    Pair(
                            plugin.core.serviceManager.getServiceProvider(RPKSkillTypeProvider::class).getSkillType(entry.key)!!,
                            entry.value
                    )
                }
                .toMap()

    override fun hasPrerequisites(character: RPKCharacter): Boolean {
        val classProvider = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class)
        for ((clazz, level) in prerequisites) {
            if (classProvider.getLevel(character, clazz) < level) {
                return false
            }
        }
        return true
    }

    override fun getSkillPoints(skillType: RPKSkillType, level: Int): Int {
        return baseSkillPoints[skillType]?:0 + (levelSkillPoints[skillType]?:0 * level)
    }

}