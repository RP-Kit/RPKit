/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.characters.bukkit.gender.RPKGender
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.stats.bukkit.command.stats.StatsCommand
import com.rpkit.stats.bukkit.stat.RPKStatProviderImpl
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableProviderImpl
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack

/**
 * RPK stats plugin default implementation.
 */
class RPKStatsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        val statVariableProvider = RPKStatVariableProviderImpl(this)
        val statProvider = RPKStatProviderImpl(this)
        serviceProviders = arrayOf(
                statVariableProvider,
                statProvider
        )
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "character"
            override fun get(character: RPKCharacter): RPKCharacter {
                return character
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "player"
            override fun get(character: RPKCharacter): RPKPlayer? {
                return character.player
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "profile"
            override fun get(character: RPKCharacter): RPKProfile? {
                return character.profile
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "minecraftProfile"
            override fun get(character: RPKCharacter): RPKMinecraftProfile? {
                return character.minecraftProfile
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "minecraftLevel"
            override fun get(character: RPKCharacter): Int {
                val minecraftProfile = character.minecraftProfile
                if (minecraftProfile != null) {
                    return server.getPlayer(minecraftProfile.minecraftUUID)?.level?:0
                }
                return 0
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterGender"
            override fun get(character: RPKCharacter): RPKGender? {
                return character.gender
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterAge"
            override fun get(character: RPKCharacter): Int {
                return character.age
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterRace"
            override fun get(character: RPKCharacter): RPKRace? {
                return character.race
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterHelmet"
            override fun get(character: RPKCharacter): ItemStack? {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        if (bukkitPlayer != null) {
                            bukkitPlayer.inventory.helmet
                        } else {
                            character.helmet
                        }
                    } else {
                        character.helmet
                    }
                } else {
                    character.helmet
                }
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterChestplate"
            override fun get(character: RPKCharacter): ItemStack? {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        if (bukkitPlayer != null) {
                            bukkitPlayer.inventory.chestplate
                        } else {
                            character.chestplate
                        }
                    } else {
                        character.chestplate
                    }
                } else {
                    character.chestplate
                }
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterLeggings"
            override fun get(character: RPKCharacter): ItemStack? {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        if (bukkitPlayer != null) {
                            bukkitPlayer.inventory.leggings
                        } else {
                            character.leggings
                        }
                    } else {
                        character.leggings
                    }
                } else {
                    character.leggings
                }
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterBoots"
            override fun get(character: RPKCharacter): ItemStack? {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        if (bukkitPlayer != null) {
                            bukkitPlayer.inventory.boots
                        } else {
                            character.boots
                        }
                    } else {
                        character.boots
                    }
                } else {
                    character.boots
                }
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterHealth"
            override fun get(character: RPKCharacter): Double {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
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
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterMaxHealth"
            override fun get(character: RPKCharacter): Double {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
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
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterMana"
            override fun get(character: RPKCharacter): Int {
                return character.mana
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterMaxMana"
            override fun get(character: RPKCharacter): Int {
                return character.maxMana
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterFoodLevel"
            override fun get(character: RPKCharacter): Int {
                val minecraftProfile = character.minecraftProfile
                return if (minecraftProfile != null) {
                    if (core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getActiveCharacter(minecraftProfile) == character) {
                        val bukkitPlayer = server.getPlayer(minecraftProfile.minecraftUUID)
                        bukkitPlayer?.foodLevel ?: character.foodLevel
                    } else {
                        character.foodLevel
                    }
                } else {
                    character.foodLevel
                }
            }
        })
        statVariableProvider.addStatVariable(object: RPKStatVariable {
            override val name = "characterThirstLevel"
            override fun get(character: RPKCharacter): Int {
                return character.thirstLevel
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
    }

}