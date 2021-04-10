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

package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.event.experience.RPKBukkitExperienceChangeEvent
import java.util.concurrent.CompletableFuture


class RPKExperienceServiceImpl(override val plugin: RPKExperienceBukkit) : RPKExperienceService {

    override fun getLevel(character: RPKCharacter): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            val experience = getExperience(character).join()
            var level = 1
            while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= experience) {
                level++
            }
            return@supplyAsync level
        }
    }

    override fun setLevel(character: RPKCharacter, level: Int): CompletableFuture<Void> {
        return setExperience(character, getExperienceNeededForLevel(level))
    }

    override fun getExperience(character: RPKCharacter): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            val experienceTable = plugin.database.getTable(RPKExperienceTable::class.java)
            val experienceValue = experienceTable[character]
            return@supplyAsync experienceValue.join()?.value ?: 0
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

                    }
                }

            }
        }
    }

    override fun getExperienceNeededForLevel(level: Int): Int {
        val expressionService = Services[RPKExpressionService::class.java] ?: return Int.MAX_VALUE
        val expression = expressionService.createExpression(plugin.config.getString("experience.formula") ?: return Int.MAX_VALUE)
        return expression.parseInt(mapOf(
            "level" to level.toDouble()
        )) ?: Int.MAX_VALUE
    }

}