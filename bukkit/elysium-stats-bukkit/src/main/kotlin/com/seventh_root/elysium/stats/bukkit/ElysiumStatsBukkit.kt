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

package com.seventh_root.elysium.stats.bukkit

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGender
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRace
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.stats.bukkit.command.stats.StatsCommand
import com.seventh_root.elysium.stats.bukkit.stat.ElysiumStatProviderImpl
import com.seventh_root.elysium.stats.bukkit.stat.ElysiumStatVariable
import com.seventh_root.elysium.stats.bukkit.stat.ElysiumStatVariableProviderImpl
import org.bukkit.inventory.ItemStack


class ElysiumStatsBukkit: ElysiumBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        val statVariableProvider = ElysiumStatVariableProviderImpl(this)
        val statProvider = ElysiumStatProviderImpl(this)
        serviceProviders = arrayOf(
                statVariableProvider,
                statProvider
        )
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "character"
            override fun get(character: ElysiumCharacter): ElysiumCharacter {
                return character
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "player"
            override fun get(character: ElysiumCharacter): ElysiumPlayer? {
                return character.player
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "minecraftLevel"
            override fun get(character: ElysiumCharacter): Int {
                return character.player?.bukkitPlayer?.player?.level?:0
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterGender"
            override fun get(character: ElysiumCharacter): ElysiumGender? {
                return character.gender
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterAge"
            override fun get(character: ElysiumCharacter): Int {
                return character.age
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterRace"
            override fun get(character: ElysiumCharacter): ElysiumRace? {
                return character.race
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterHelmet"
            override fun get(character: ElysiumCharacter): ItemStack? {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.inventory?.helmet ?: character.helmet
                    } else {
                        return character.helmet
                    }
                } else {
                    return character.helmet
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterChestplate"
            override fun get(character: ElysiumCharacter): ItemStack? {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.inventory?.chestplate ?: character.chestplate
                    } else {
                        return character.chestplate
                    }
                } else {
                    return character.chestplate
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterLeggings"
            override fun get(character: ElysiumCharacter): ItemStack? {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.inventory?.leggings ?: character.leggings
                    } else {
                        return character.leggings
                    }
                } else {
                    return character.leggings
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterBoots"
            override fun get(character: ElysiumCharacter): ItemStack? {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.inventory?.boots ?: character.boots
                    } else {
                        return character.boots
                    }
                } else {
                    return character.boots
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterHealth"
            override fun get(character: ElysiumCharacter): Double {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.health ?: character.health
                    } else {
                        return character.health
                    }
                } else {
                    return character.health
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterMaxHealth"
            override fun get(character: ElysiumCharacter): Double {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.maxHealth ?: character.maxHealth
                    } else {
                        return character.maxHealth
                    }
                } else {
                    return character.maxHealth
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterMana"
            override fun get(character: ElysiumCharacter): Int {
                return character.mana
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterMaxMana"
            override fun get(character: ElysiumCharacter): Int {
                return character.maxMana
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterFoodLevel"
            override fun get(character: ElysiumCharacter): Int {
                val player = character.player
                if (player != null) {
                    if (core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getActiveCharacter(player) == character) {
                        return character.player?.bukkitPlayer?.player?.foodLevel ?: character.foodLevel
                    } else {
                        return character.foodLevel
                    }
                } else {
                    return character.foodLevel
                }
            }
        })
        statVariableProvider.addStatVariable(object: ElysiumStatVariable {
            override val name = "characterThirstLevel"
            override fun get(character: ElysiumCharacter): Int {
                return character.thirstLevel
            }
        })
    }

    override fun registerCommands() {
        getCommand("stats").executor = StatsCommand(this)
    }

}