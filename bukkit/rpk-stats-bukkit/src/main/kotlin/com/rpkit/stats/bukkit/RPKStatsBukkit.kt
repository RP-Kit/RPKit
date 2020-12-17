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

package com.rpkit.stats.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.service.Services
import com.rpkit.stats.bukkit.command.stats.StatsCommand
import com.rpkit.stats.bukkit.stat.RPKStatService
import com.rpkit.stats.bukkit.stat.RPKStatServiceImpl
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableService
import com.rpkit.stats.bukkit.stat.RPKStatVariableServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.attribute.Attribute

/**
 * RPK stats plugin default implementation.
 */
class RPKStatsBukkit : RPKBukkitPlugin() {

    override fun onEnable() {
        System.setProperty("com.rpkit.stats.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4419)
        saveDefaultConfig()
        val statVariableService = RPKStatVariableServiceImpl(this)
        val statService = RPKStatServiceImpl(this)
        Services[RPKStatVariableService::class.java] = statVariableService
        Services[RPKStatService::class.java] = statService
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "minecraftLevel"
            override fun get(character: RPKCharacter): Double {
                val minecraftProfile = character.minecraftProfile
                if (minecraftProfile != null) {
                    return server.getPlayer(minecraftProfile.minecraftUUID)?.level?.toDouble() ?: 0.0
                }
                return 0.0
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterAge"
            override fun get(character: RPKCharacter): Double {
                return character.age.toDouble()
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterHealth"
            override fun get(character: RPKCharacter): Double {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (Services[RPKCharacterService::class.java]?.getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        bukkitPlayer?.health ?: character.health
                    } else {
                        character.health
                    }
                } else {
                    character.health
                }
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterMaxHealth"
            override fun get(character: RPKCharacter): Double {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (Services[RPKCharacterService::class.java]?.getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        if (bukkitPlayer != null) {
                            bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: character.maxHealth
                        } else {
                            character.maxHealth
                        }
                    } else {
                        character.maxHealth
                    }
                } else {
                    character.maxHealth
                }
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterMana"
            override fun get(character: RPKCharacter): Double {
                return character.mana.toDouble()
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterMaxMana"
            override fun get(character: RPKCharacter): Double {
                return character.maxMana.toDouble()
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterFoodLevel"
            override fun get(character: RPKCharacter): Double {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (Services[RPKCharacterService::class.java]?.getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        bukkitPlayer?.foodLevel?.toDouble() ?: character.foodLevel.toDouble()
                    } else {
                        character.foodLevel.toDouble()
                    }
                } else {
                    character.foodLevel.toDouble()
                }
            }
        })
        statVariableService.addStatVariable(object : RPKStatVariable {
            override val name = "characterThirstLevel"
            override fun get(character: RPKCharacter): Double {
                return character.thirstLevel.toDouble()
            }
        })
    }

    override fun registerCommands() {
        getCommand("stats")?.setExecutor(StatsCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("stats-list-title", "&fStats:")
        messages.setDefault("stats-list-item", "&7\$stat: &f\$value")
        messages.setDefault("no-character", "&cYou must have an active character in order to perform this command.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform this command.")
        messages.setDefault("no-permission-stats", "&cYou do not have permission to view your stats.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-stats-service", "&cThere is no stats service available.")
        messages.setDefault("no-stat-variable-service", "&cThere is no stat variable service available.")
    }

}