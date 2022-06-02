/*
 * Copyright 2022 Ren Binden
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level


class RPKClassServiceImpl(override val plugin: RPKClassesBukkit) : RPKClassService {

    override val classes: List<RPKClass> = plugin.config.getConfigurationSection("classes")
        ?.getKeys(false)
        ?.map { className ->
            RPKClassImpl(
                plugin,
                RPKClassName(className),
                plugin.config.getInt("classes.$className.max-level"),
                plugin.config.getInt("classes.$className.max-age", Int.MAX_VALUE),
                plugin.config.getInt("classes.$className.min-age", 0),
                plugin.config.getConfigurationSection("classes.$className.prerequisites")
                    ?.getKeys(false)?.associate { prerequisiteClassName ->
                        prerequisiteClassName to plugin.config.getInt("classes.$className.prerequisites.$prerequisiteClassName")
                    } ?: mapOf(),
                plugin.config.getConfigurationSection("classes.$className.skill-points.base")
                    ?.getKeys(false)?.associate { skillTypeName ->
                        skillTypeName to plugin.config.getInt("classes.$className.skill-points.base.$skillTypeName")
                    } ?: mapOf(),
                plugin.config.getConfigurationSection("classes.$className.skill-points.level")
                    ?.getKeys(false)?.associate { skillTypeName ->
                        skillTypeName to plugin.config.getInt("classes.$className.skill-points.level.$skillTypeName")
                    } ?: mapOf(),
                plugin.config.getConfigurationSection("classes.$className.stat-variables")
                    ?.getKeys(false)?.associate { statVariableName ->
                        statVariableName to (plugin.config.getString("classes.$className.stat-variables.$statVariableName")
                            ?: "0")
                    } ?: mapOf()
            )
        }
        ?: mutableListOf()

    private val characterClasses = ConcurrentHashMap<Int, RPKClass>()
    private val characterClassExperience = ConcurrentHashMap<Int, ConcurrentHashMap<RPKClass, Int>>()

    override fun getClass(name: RPKClassName): RPKClass? {
        return classes.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }
    }

    override fun getClass(character: RPKCharacter): CompletableFuture<RPKClass?> {
        val preloadedClass = getPreloadedClass(character)
        if (preloadedClass != null) return CompletableFuture.completedFuture(preloadedClass)
        return plugin.database.getTable(RPKCharacterClassTable::class.java)[character]
            .thenApply { characterClass -> characterClass?.`class` }
    }

    override fun setClass(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Void> {
        // Update experience in the class the character is being switched from
        // Unsafe if experience service is unavailable so just abort
        val experienceService =
            Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(null)
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
                .join()

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
                characterClass.`class` = event.`class`
                characterClassTable.update(characterClass).join()
            }
            if (character.minecraftProfile?.isOnline == true) {
                val characterId = character.id
                if (characterId != null) {
                    characterClasses[characterId.value] = event.`class`
                    if (eventOldClass != null) {
                        unloadExperience(character, eventOldClass)
                    }
                    loadExperience(character, event.`class`).join()
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set class", exception)
            throw exception
        }
    }

    override fun getPreloadedClass(character: RPKCharacter): RPKClass? {
        return characterClasses[character.id?.value]
    }

    override fun loadClass(character: RPKCharacter): CompletableFuture<RPKClass?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val preloadedClass = getPreloadedClass(character)
        if (preloadedClass != null) return CompletableFuture.completedFuture(preloadedClass)
        plugin.logger.info("Loading class for character ${character.name} (${characterId.value})...")
        val characterClassFuture = plugin.database.getTable(RPKCharacterClassTable::class.java)[character]
        characterClassFuture.thenAccept { characterClass ->
            if (characterClass != null) {
                characterClasses[characterId.value] = characterClass.`class`
                plugin.logger.info("Loaded class for character ${character.name} (${characterId.value}): ${characterClass.`class`.name.value}")
            }
        }
        return characterClassFuture.thenApply { it?.`class` }
    }

    override fun unloadClass(character: RPKCharacter) {
        val characterId = character.id ?: return
        characterClasses.remove(characterId.value)
        plugin.logger.info("Unloaded class for character ${character.name} (${characterId.value})")
    }

    private fun getLevelForExperience(experience: Int, maxLevel: Int): Int {
        val experienceService = Services[RPKExperienceService::class.java] ?: return 1
        var level = 1
        while (level + 1 <= maxLevel && experienceService.getExperienceNeededForLevel(level + 1) <= experience) {
            level++
        }
        return level
    }

    override fun getLevel(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Int> {
        val experienceService =
            Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(1)
        return CompletableFuture.supplyAsync {
            if (`class` == getClass(character).join()) {
                return@supplyAsync experienceService.getLevel(character).join()
            } else {
                val experience = getExperience(character, `class`).join()
                return@supplyAsync getLevelForExperience(experience, `class`.maxLevel)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get level", exception)
            throw exception
        }
    }

    override fun setLevel(character: RPKCharacter, `class`: RPKClass, level: Int): CompletableFuture<Void> {
        val experienceService =
            Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            if (`class` == getClass(character).join()) {
                experienceService.setLevel(character, level).join()
            } else {
                setExperience(character, `class`, experienceService.getExperienceNeededForLevel(level)).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set level", exception)
            throw exception
        }
    }

    override fun getPreloadedLevel(character: RPKCharacter, `class`: RPKClass): Int? {
        return getPreloadedExperience(character, `class`)?.let { getLevelForExperience(it, `class`.maxLevel) }
    }

    override fun getExperience(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Int> {
        val experienceService =
            Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(0)
        return CompletableFuture.supplyAsync {
            return@supplyAsync if (`class` == getClass(character).join()) {
                experienceService.getExperience(character).join()
            } else {
                val classExperienceTable = plugin.database.getTable(RPKClassExperienceTable::class.java)
                val classExperience = classExperienceTable[character, `class`].join()
                classExperience?.experience ?: 0
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get class experience", exception)
            throw exception
        }
    }

    override fun setExperience(character: RPKCharacter, `class`: RPKClass, experience: Int): CompletableFuture<Void> {
        val experienceService =
            Services[RPKExperienceService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val oldExperience = getExperience(character, `class`).join()
            val event = RPKBukkitClassExperienceChangeEvent(character, `class`, oldExperience, experience, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            if (`class` == getClass(character).join()) {
                experienceService.setExperience(character, experience).join()
                if (character.minecraftProfile?.isOnline == true) {
                    val characterId = character.id
                    if (characterId != null) {
                        val characterClassExperienceValues =
                            characterClassExperience[characterId.value] ?: ConcurrentHashMap()
                        characterClassExperienceValues[`class`] = experience
                        characterClassExperience[characterId.value] = characterClassExperienceValues
                    }
                }
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
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set class experience", exception)
            throw exception
        }
    }

    override fun getPreloadedExperience(character: RPKCharacter, `class`: RPKClass): Int? {
        val characterId = character.id ?: return null
        return characterClassExperience[characterId.value]?.get(`class`)
    }

    override fun loadExperience(character: RPKCharacter, `class`: RPKClass): CompletableFuture<Int> {
        val preloadedExperience = getPreloadedExperience(character, `class`)
        if (preloadedExperience != null) return CompletableFuture.completedFuture(preloadedExperience)
        val characterId = character.id ?: return CompletableFuture.completedFuture(0)
        plugin.logger.info("Loading character class experience for character ${character.name} (${characterId.value}), class ${`class`.name.value}...")
        return CompletableFuture.supplyAsync {
            val experience = getExperience(character, `class`).join()
            val characterClassExperienceValues = characterClassExperience[characterId.value] ?: ConcurrentHashMap()
            characterClassExperienceValues[`class`] = experience
            characterClassExperience[characterId.value] = characterClassExperienceValues
            plugin.logger.info("Loaded character class experience for character ${character.name} (${characterId.value}), class ${`class`.name.value}: $experience")
            return@supplyAsync experience
        }
    }

    override fun unloadExperience(character: RPKCharacter, `class`: RPKClass) {
        val characterId = character.id ?: return
        val characterClassExperienceValues = characterClassExperience[characterId.value] ?: return
        characterClassExperienceValues.remove(`class`)
        characterClassExperience[characterId.value] = characterClassExperienceValues
        plugin.logger.info("Unloaded character class experience for character ${character.name} (${characterId.value}), class ${`class`.name.value}")
    }
}
