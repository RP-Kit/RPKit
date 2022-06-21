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

package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.event.experience.RPKBukkitExperienceChangeEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level


class RPKExperienceServiceImpl(override val plugin: RPKExperienceBukkit) : RPKExperienceService {

    private val experience = ConcurrentHashMap<Int, Int>()

    override fun getLevel(character: RPKCharacter): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            val experience = getExperience(character).join()
            return@supplyAsync getLevel(experience)
        }
    }

    override fun setLevel(character: RPKCharacter, level: Int): CompletableFuture<Void> {
        return setExperience(character, getExperienceNeededForLevel(level))
    }

    override fun getExperience(character: RPKCharacter): CompletableFuture<Int> {
        val preloadedExperience = getPreloadedExperience(character)
        if (preloadedExperience != null) return CompletableFuture.completedFuture(preloadedExperience)
        return CompletableFuture.supplyAsync {
            val experienceTable = plugin.database.getTable(RPKExperienceTable::class.java)
            val experienceValue = experienceTable[character]
            return@supplyAsync experienceValue.join()?.value ?: 0
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get experience", exception)
            throw exception
        }
    }

    override fun setExperience(character: RPKCharacter, experience: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitExperienceChangeEvent(character, getExperience(character).join(), experience, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val experienceTable = plugin.database.getTable(RPKExperienceTable::class.java)
            experienceTable[character].thenAccept { experienceValue ->
                if (experienceValue == null) {
                    experienceTable.insert(RPKExperienceValue(character = character, value = event.experience))
                } else {
                    experienceValue.value = event.experience
                    experienceTable.update(experienceValue)
                }.thenRun {
                    var level = 1
                    while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= event.experience) {
                        level++
                    }
                    val isMaxLevel = level == plugin.config.getInt("levels.max-level")
                    val minecraftProfile = character.minecraftProfile
                    if (minecraftProfile != null) {
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
                            if (bukkitPlayer != null) {
                                bukkitPlayer.level = level
                                if (isMaxLevel) {
                                    bukkitPlayer.exp = 0F
                                } else {
                                    bukkitPlayer.exp =
                                        (event.experience - getExperienceNeededForLevel(level)).toFloat() / (getExperienceNeededForLevel(
                                            level + 1
                                        ) - getExperienceNeededForLevel(level)).toFloat()
                                }
                            }
                        })
                        if (minecraftProfile.isOnline) {
                            val characterId = character.id
                            if (characterId != null) {
                                this.experience[characterId.value] = experience
                            }
                        }
                    }
                }

            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set experience", exception)
            throw exception
        }
    }

    override fun getExperienceNeededForLevel(level: Int): Int {
        val expressionService = Services[RPKExpressionService::class.java] ?: return Int.MAX_VALUE
        val expression = expressionService.createExpression(plugin.config.getString("experience.formula") ?: return Int.MAX_VALUE)
        return expression.parseInt(mapOf(
            "level" to level.toDouble()
        )) ?: Int.MAX_VALUE
    }

    override fun loadExperience(character: RPKCharacter): CompletableFuture<Int> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(0)
        val preloadedExperience = experience[characterId.value]
        if (preloadedExperience != null) return CompletableFuture.completedFuture(preloadedExperience)
        val experienceFuture = plugin.database.getTable(RPKExperienceTable::class.java)[character]
        plugin.logger.info("Loading experience for character ${character.name} (${characterId.value})...")
        experienceFuture.thenAccept { experience ->
            if (experience == null) return@thenAccept
            this.experience[characterId.value] = experience.value
            plugin.logger.info("Loaded experience for character ${character.name} (${characterId.value}): ${experience.value}")
        }
        return experienceFuture.thenApply { it?.value }
    }

    override fun unloadExperience(character: RPKCharacter) {
        val characterId = character.id ?: return
        experience.remove(characterId.value)
        plugin.logger.info("Unloaded experience for character ${character.name} (${characterId.value})")
    }

    override fun getPreloadedExperience(character: RPKCharacter): Int? {
        val characterId = character.id ?: return null
        return experience[characterId.value]
    }

    override fun getPreloadedLevel(character: RPKCharacter): Int? {
        return getPreloadedExperience(character)?.let(::getLevel)
    }

    private fun getLevel(experience: Int): Int {
        var level = 1
        while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= experience) {
            level++
        }
        return level
    }

}