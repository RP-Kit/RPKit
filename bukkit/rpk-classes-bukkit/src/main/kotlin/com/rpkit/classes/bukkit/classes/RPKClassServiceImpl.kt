/*
 * Copyright 2021 Ren Binden
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
import java.util.concurrent.CompletableFuture


class RPKClassServiceImpl(override val plugin: RPKClassesBukkit) : RPKClassService {

    override val classes: List<RPKClass> = plugin.config.getConfigurationSection("classes")
            ?.getKeys(false)
            ?.map { className ->
                RPKClassImpl(
                        RPKClassName(className),
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

    override fun getClass(name: RPKClassName): RPKClass? {
        return classes.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }
    }

    override fun getClass(character: RPKCharacter): CompletableFuture<RPKClass?> {
        return plugin.database.getTable(RPKCharacterClassTable::class.java)[character]
            .thenApply { characterClass -> characterClass?.`class` }
    }

    override fun setClass(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Void> {
        // Update experience in the class the character is being switched from
        // Unsafe if experience service is unavailable so just abort
        val experienceService = Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val oldClass = getClass(character).join()
            val event = RPKBukkitClassChangeEvent(character, oldClass, `class`, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val eventOldClass = event.oldClass
            if (eventOldClass != null) {
                experienceService.getExperience(event.character).thenAcceptAsync { experience ->
                    val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
                    var classExperience = classExperienceTable[event.character, eventOldClass].join()
                    if (classExperience == null) {
                        classExperience = RPKClassExperience(
                            character = event.character,
                            `class` = eventOldClass,
                            experience = experience
                        )
                        classExperienceTable.insert(classExperience).join()
                    } else {
                        classExperience.experience = experience
                        classExperienceTable.update(classExperience).join()
                    }
                }.join()
            }

            // Update experience in the experience service to that of the new class
            experienceService.setExperience(event.character, getExperience(event.character, event.`class`).join())

            // Update database with new class
            val characterClassTable = plugin.database.getTable(RPKCharacterClassTable::class.java)
            var characterClass = characterClassTable[event.character].join()
            if (characterClass == null) {
                characterClass = RPKCharacterClass(
                    character = event.character,
                    `class` = event.`class`
                )
                characterClassTable.insert(characterClass).join()
            } else {
                characterClass.`class` = `class`
                characterClassTable.update(characterClass).join()
            }
        }
    }

    override fun getLevel(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Int> {
        val experienceService = Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(1)
        return CompletableFuture.supplyAsync {
            if (`class` == getClass(character).join()) {
                experienceService.getLevel(character).join()
            } else {
                val experience = getExperience(character, `class`).join()
                var level = 1
                while (level + 1 <= `class`.maxLevel && experienceService.getExperienceNeededForLevel(level + 1) <= experience) {
                    level++
                }
                level
            }
        }
    }

    override fun setLevel(character: RPKCharacter, `class`: RPKClass, level: Int): CompletableFuture<Void> {
        val experienceService = Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            if (`class` == getClass(character).join()) {
                experienceService.setLevel(character, level).join()
            } else {
                setExperience(character, `class`, experienceService.getExperienceNeededForLevel(level)).join()
            }
        }
    }

    override fun getExperience(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Int> {
        val experienceService = Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(0)
        return CompletableFuture.supplyAsync {
            return@supplyAsync if (`class` == getClass(character).join()) {
                experienceService.getExperience(character).join()
            } else {
                val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
                val classExperience = classExperienceTable[character, `class`].join()
                classExperience?.experience ?: 0
            }
        }
    }

    override fun setExperience(character: RPKCharacter, `class`: RPKClass, experience: Int): CompletableFuture<Void> {
        val experienceService = Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val oldExperience = getExperience(character, `class`).join()
            val event = RPKBukkitClassExperienceChangeEvent(character, `class`, oldExperience, experience, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            if (`class` == getClass(character).join()) {
                experienceService.setExperience(character, experience).join()
            } else {
                val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
                var classExperience = classExperienceTable[character, `class`].join()
                if (classExperience == null) {
                    classExperience = RPKClassExperience(
                        character = character,
                        `class` = `class`,
                        experience = experience
                    )
                    classExperienceTable.insert(classExperience).join()
                } else {
                    classExperience.experience = experience
                    classExperienceTable.update(classExperience).join()
                }
            }
        }
    }
}