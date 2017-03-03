package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.database.table.RPKCharacterClassTable
import com.rpkit.classes.bukkit.database.table.RPKClassExperienceTable
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider


class RPKClassProviderImpl(private val plugin: RPKClassesBukkit): RPKClassProvider {

    override val classes: List<RPKClass> = plugin.config.getConfigurationSection("classes").getKeys(false)
            .map { className ->
                    RPKClassImpl(
                            plugin,
                            className,
                            plugin.config.getInt("classes.$className.max-level"),
                            plugin.config.getConfigurationSection("classes.$className.prerequisites")
                                    ?.getKeys(false)
                                    ?.map { prerequisiteClassName ->
                                        Pair(
                                                prerequisiteClassName,
                                                plugin.config.getInt("classes.$className.prerequisites.$prerequisiteClassName")
                                        )
                                    }
                                    ?.toMap()
                                    ?:mapOf(),
                            plugin.config.getConfigurationSection("classes.$className.skill-points.base")
                                    .getKeys(false)
                                    .map { skillTypeName ->
                                        Pair(
                                                skillTypeName,
                                                plugin.config.getInt("classes.$className.skill-points.base.$skillTypeName")
                                        )
                                    }
                                    .toMap(),
                            plugin.config.getConfigurationSection("classes.$className.skill-points.level")
                                    .getKeys(false)
                                    .map { skillTypeName ->
                                        Pair(
                                                skillTypeName,
                                                plugin.config.getInt("classes.$className.skill-points.level.$skillTypeName")
                                        )
                                    }
                                    .toMap()
                    )
            }

    override fun getClass(name: String): RPKClass? {
        return classes.filter { it.name.equals(name, ignoreCase = true) }.firstOrNull()
    }

    override fun getClass(character: RPKCharacter): RPKClass? {
        return plugin.core.database.getTable(RPKCharacterClassTable::class).get(character)?.clazz
    }

    override fun setClass(character: RPKCharacter, clazz: RPKClass) {
        // Update experience in the class the character is being switched from
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        val oldClass = getClass(character)
        if (oldClass != null) {
            val experience = experienceProvider.getExperience(character)
            val classExperienceTable = plugin.core.database.getTable(RPKClassExperienceTable::class)
            var classExperience = classExperienceTable.get(character, oldClass)
            if (classExperience == null) {
                classExperience = RPKClassExperience(
                        character = character,
                        clazz = oldClass,
                        experience = experience
                )
                classExperienceTable.insert(classExperience)
            } else {
                classExperience.experience = experience
                classExperienceTable.update(classExperience)
            }
        }

        // Update experience in the experience provider to that of the new class
        experienceProvider.setExperience(character, getExperience(character, clazz))

        // Update database with new class
        val characterClassTable = plugin.core.database.getTable(RPKCharacterClassTable::class)
        var characterClass = characterClassTable.get(character)
        if (characterClass == null) {
            characterClass = RPKCharacterClass(
                    character = character,
                    clazz = clazz
            )
            characterClassTable.insert(characterClass)
        } else {
            characterClass.clazz = clazz
            characterClassTable.update(characterClass)
        }
    }

    override fun getLevel(character: RPKCharacter, clazz: RPKClass): Int {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (clazz == getClass(character)) {
            return experienceProvider.getLevel(character)
        } else {
            val experience = getExperience(character, clazz)
            var level = 1
            while (level + 1 <= clazz.maxLevel && experienceProvider.getExperienceNeededForLevel(level + 1) <= experience) {
                level++
            }
            return level
        }
    }

    override fun setLevel(character: RPKCharacter, clazz: RPKClass, level: Int) {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (clazz == getClass(character)) {
            experienceProvider.setLevel(character, level)
        } else {
            setExperience(character, clazz, experienceProvider.getExperienceNeededForLevel(level))
        }
    }

    override fun getExperience(character: RPKCharacter, clazz: RPKClass): Int {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (clazz == getClass(character)) {
            return experienceProvider.getExperience(character)
        } else {
            val classExperienceTable = plugin.core.database.getTable(RPKClassExperienceTable::class)
            val classExperience = classExperienceTable.get(character, clazz)
            return classExperience?.experience?:0
        }
    }

    override fun setExperience(character: RPKCharacter, clazz: RPKClass, experience: Int) {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (clazz == getClass(character)) {
            experienceProvider.setExperience(character, experience)
        } else {
            val classExperienceTable = plugin.core.database.getTable(RPKClassExperienceTable::class)
            var classExperience = classExperienceTable.get(character, clazz)
            if (classExperience == null) {
                classExperience = RPKClassExperience(
                        character = character,
                        clazz = clazz,
                        experience = experience
                )
                classExperienceTable.insert(classExperience)
            } else {
                classExperience.experience = experience
                classExperienceTable.update(classExperience)
            }
        }
    }
}