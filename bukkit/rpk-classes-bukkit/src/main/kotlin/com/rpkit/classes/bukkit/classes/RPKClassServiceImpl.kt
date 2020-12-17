/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.database.table.RPKCharacterClassTable
import com.rpkit.classes.bukkit.database.table.RPKClassExperienceTable
import com.rpkit.classes.bukkit.event.`class`.RPKBukkitClassChangeEvent
import com.rpkit.classes.bukkit.event.`class`.RPKBukkitClassExperienceChangeEvent
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.experience.RPKExperienceService


class RPKClassServiceImpl(override val plugin: RPKClassesBukkit) : RPKClassService {

    override val classes: List<RPKClass> = plugin.config.getConfigurationSection("classes")
            ?.getKeys(false)
            ?.map { className ->
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
                                ?: mapOf(),
                        plugin.config.getConfigurationSection("classes.$className.skill-points.base")
                                ?.getKeys(false)
                                ?.map { skillTypeName ->
                                    Pair(
                                            skillTypeName,
                                            plugin.config.getInt("classes.$className.skill-points.base.$skillTypeName")
                                    )
                                }
                                ?.toMap()
                                ?: mapOf(),
                        plugin.config.getConfigurationSection("classes.$className.skill-points.level")
                                ?.getKeys(false)
                                ?.map { skillTypeName ->
                                    Pair(
                                            skillTypeName,
                                            plugin.config.getInt("classes.$className.skill-points.level.$skillTypeName")
                                    )
                                }
                                ?.toMap()
                                ?: mapOf(),
                        plugin.config.getConfigurationSection("classes.$className.stat-variables")
                                ?.getKeys(false)
                                ?.map { statVariableName ->
                                    Pair(
                                            statVariableName,
                                            plugin.config.getString("classes.$className.stat-variables.$statVariableName")
                                                    ?: "0"
                                    )
                                }
                                ?.toMap()
                                ?: mapOf()
                )
            }
            ?: mutableListOf()

    override fun getClass(name: String): RPKClass? {
        return classes.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    override fun getClass(character: RPKCharacter): RPKClass? {
        return plugin.database.getTable(RPKCharacterClassTable::class.java).get(character)?.`class`
    }

    override fun setClass(character: RPKCharacter, `class`: RPKClass) {
        // Update experience in the class the character is being switched from
        // Unsafe if experience service is unavailable so just abort
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        val oldClass = getClass(character)
        val event = RPKBukkitClassChangeEvent(character, oldClass, `class`)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventOldClass = event.oldClass
        if (eventOldClass != null) {
            val experience = experienceService.getExperience(event.character)
            val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
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

        // Update experience in the experience service to that of the new class
        experienceService.setExperience(event.character, getExperience(event.character, event.`class`))

        // Update database with new class
        val characterClassTable = plugin.database.getTable(RPKCharacterClassTable::class.java)
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
        val experienceService = Services[RPKExperienceService::class.java] ?: return 1
        return if (`class` == getClass(character)) {
            experienceService.getLevel(character)
        } else {
            val experience = getExperience(character, `class`)
            var level = 1
            while (level + 1 <= `class`.maxLevel && experienceService.getExperienceNeededForLevel(level + 1) <= experience) {
                level++
            }
            level
        }
    }

    override fun setLevel(character: RPKCharacter, `class`: RPKClass, level: Int) {
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        if (`class` == getClass(character)) {
            experienceService.setLevel(character, level)
        } else {
            setExperience(character, `class`, experienceService.getExperienceNeededForLevel(level))
        }
    }

    override fun getExperience(character: RPKCharacter, `class`: RPKClass): Int {
        val experienceService = Services[RPKExperienceService::class.java] ?: return 0
        return if (`class` == getClass(character)) {
            experienceService.getExperience(character)
        } else {
            val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
            val classExperience = classExperienceTable.get(character, `class`)
            classExperience?.experience ?: 0
        }
    }

    override fun setExperience(character: RPKCharacter, `class`: RPKClass, experience: Int) {
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        val oldExperience = getExperience(character, `class`)
        val event = RPKBukkitClassExperienceChangeEvent(character, `class`, oldExperience, experience)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        if (`class` == getClass(character)) {
            experienceService.setExperience(character, experience)
        } else {
            val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
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