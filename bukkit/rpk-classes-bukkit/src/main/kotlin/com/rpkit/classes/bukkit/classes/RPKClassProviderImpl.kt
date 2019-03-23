package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.database.table.RPKCharacterClassTable
import com.rpkit.classes.bukkit.database.table.RPKClassExperienceTable
import com.rpkit.classes.bukkit.event.`class`.RPKBukkitClassChangeEvent
import com.rpkit.classes.bukkit.event.`class`.RPKBukkitClassExperienceChangeEvent
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
        return plugin.core.database.getTable(RPKCharacterClassTable::class).get(character)?.`class`
    }

    override fun setClass(character: RPKCharacter, `class`: RPKClass) {
        // Update experience in the class the character is being switched from
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        val oldClass = getClass(character)
        val event = RPKBukkitClassChangeEvent(character, oldClass, `class`)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventOldClass = event.oldClass
        if (eventOldClass != null) {
            val experience = experienceProvider.getExperience(event.character)
            val classExperienceTable = plugin.core.database.getTable(RPKClassExperienceTable::class)
            var classExperience = classExperienceTable.get(event.character, eventOldClass)
            if (classExperience == null) {
                classExperience = RPKClassExperience(
                        character = event.character,
                        `class` = eventOldClass,
                        experience = experience
                )
                classExperienceTable.insert(classExperience)
            } else {
                classExperience.experience = experience
                classExperienceTable.update(classExperience)
            }
        }

        // Update experience in the experience provider to that of the new class
        experienceProvider.setExperience(event.character, getExperience(event.character, event.`class`))

        // Update database with new class
        val characterClassTable = plugin.core.database.getTable(RPKCharacterClassTable::class)
        var characterClass = characterClassTable.get(event.character)
        if (characterClass == null) {
            characterClass = RPKCharacterClass(
                    character = event.character,
                    `class` = event.`class`
            )
            characterClassTable.insert(characterClass)
        } else {
            characterClass.`class` = `class`
            characterClassTable.update(characterClass)
        }
    }

    override fun getLevel(character: RPKCharacter, `class`: RPKClass): Int {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (`class` == getClass(character)) {
            return experienceProvider.getLevel(character)
        } else {
            val experience = getExperience(character, `class`)
            var level = 1
            while (level + 1 <= `class`.maxLevel && experienceProvider.getExperienceNeededForLevel(level + 1) <= experience) {
                level++
            }
            return level
        }
    }

    override fun setLevel(character: RPKCharacter, `class`: RPKClass, level: Int) {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (`class` == getClass(character)) {
            experienceProvider.setLevel(character, level)
        } else {
            setExperience(character, `class`, experienceProvider.getExperienceNeededForLevel(level))
        }
    }

    override fun getExperience(character: RPKCharacter, `class`: RPKClass): Int {
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (`class` == getClass(character)) {
            return experienceProvider.getExperience(character)
        } else {
            val classExperienceTable = plugin.core.database.getTable(RPKClassExperienceTable::class)
            val classExperience = classExperienceTable.get(character, `class`)
            return classExperience?.experience?:0
        }
    }

    override fun setExperience(character: RPKCharacter, `class`: RPKClass, experience: Int) {
        val oldExperience = getExperience(character, `class`)
        val event = RPKBukkitClassExperienceChangeEvent(character, `class`, oldExperience, experience)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        if (`class` == getClass(character)) {
            experienceProvider.setExperience(character, experience)
        } else {
            val classExperienceTable = plugin.core.database.getTable(RPKClassExperienceTable::class)
            var classExperience = classExperienceTable.get(character, `class`)
            if (classExperience == null) {
                classExperience = RPKClassExperience(
                        character = character,
                        `class` = `class`,
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