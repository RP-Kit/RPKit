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

package com.rpkit.characters.bukkit.character

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.table.RPKCharacterTable
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.attribute.Attribute

/**
 * Character provider implementation.
 */
class RPKCharacterProviderImpl(private val plugin: RPKCharactersBukkit) : RPKCharacterProvider {

    override fun getCharacter(id: Int): RPKCharacter? {
        return plugin.core.database.getTable(RPKCharacterTable::class)[id]
    }

    override fun getActiveCharacter(player: RPKPlayer): RPKCharacter? {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val offlineBukkitPlayer = player.bukkitPlayer
        if (offlineBukkitPlayer != null) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(offlineBukkitPlayer)
            if (minecraftProfile != null) {
                return getActiveCharacter(minecraftProfile)
            }
        }

        val characterTable = plugin.core.database.getTable(RPKCharacterTable::class)
        val character = characterTable.getActive(player)
        return character
    }

    override fun getActiveCharacter(minecraftProfile: RPKMinecraftProfile): RPKCharacter? {
        var character = plugin.core.database.getTable(RPKCharacterTable::class).get(minecraftProfile)
        if (character != null) {
            return character
        } else {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val player = playerProvider.getPlayer(plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID))
            val characterTable = plugin.core.database.getTable(RPKCharacterTable::class)
            character = characterTable.getActive(player)
            if (character != null) {
                setActiveCharacter(minecraftProfile, character)
                return character
            }
        }
        return null
    }

    override fun setActiveCharacter(player: RPKPlayer, character: RPKCharacter?) {
        val offlineBukkitPlayer = player.bukkitPlayer
        if (offlineBukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(offlineBukkitPlayer)
            if (minecraftProfile != null) {
                setActiveCharacter(minecraftProfile, character)
            }
        }
    }

    override fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: RPKCharacter?) {
        val oldCharacter = getActiveCharacter(minecraftProfile)
        if (oldCharacter != null) {
            oldCharacter.minecraftProfile = null
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            if (offlineBukkitPlayer != null) {
                if (offlineBukkitPlayer.isOnline) {
                    val bukkitPlayer = offlineBukkitPlayer.player
                    oldCharacter.inventoryContents = bukkitPlayer.inventory.contents
                    oldCharacter.helmet = bukkitPlayer.inventory.helmet
                    oldCharacter.chestplate = bukkitPlayer.inventory.chestplate
                    oldCharacter.leggings = bukkitPlayer.inventory.leggings
                    oldCharacter.boots = bukkitPlayer.inventory.boots
                    oldCharacter.location = bukkitPlayer.location
                    oldCharacter.health = bukkitPlayer.health
                    oldCharacter.foodLevel = bukkitPlayer.foodLevel
                }
            }
            updateCharacter(oldCharacter)
        }
        if (character != null) {
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            if (offlineBukkitPlayer != null) {
                if (offlineBukkitPlayer.isOnline) {
                    val bukkitPlayer = offlineBukkitPlayer.player
                    bukkitPlayer.inventory.contents = character.inventoryContents
                    bukkitPlayer.inventory.helmet = character.helmet
                    bukkitPlayer.inventory.chestplate = character.chestplate
                    bukkitPlayer.inventory.leggings = character.leggings
                    bukkitPlayer.inventory.boots = character.boots
                    bukkitPlayer.teleport(character.location)
                    bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue = character.maxHealth
                    bukkitPlayer.health = character.health
                    bukkitPlayer.foodLevel = character.foodLevel
                    if (plugin.config.getBoolean("characters.set-player-display-name")) {
                        bukkitPlayer.displayName = character.name
                    }
                }
            }
            character.minecraftProfile = minecraftProfile
            updateCharacter(character)
        } else if (oldCharacter != null) {
            oldCharacter.minecraftProfile = null
            updateCharacter(oldCharacter)
        }
    }

    override fun getCharacters(player: RPKPlayer): Collection<RPKCharacter> {
        val offlineBukkitPlayer = player.bukkitPlayer
        if (offlineBukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(offlineBukkitPlayer)
            if (minecraftProfile != null) {
                val profile = minecraftProfile.profile
                if (profile != null) {
                    val characters = getCharacters(profile).toMutableList()
                    val characterTable = plugin.core.database.getTable(RPKCharacterTable::class)
                    val oldCharacters = characterTable.get(player)
                    oldCharacters.forEach { oldCharacter ->
                        oldCharacter.profile = profile
                        updateCharacter(oldCharacter)
                    }
                    characters.addAll(oldCharacters)
                    if (characters.isNotEmpty()) {
                        return characters.distinct()
                    }
                }
            }
        }
        val characterTable = plugin.core.database.getTable(RPKCharacterTable::class)
        val oldCharacters = characterTable.get(player)
        return oldCharacters
    }

    override fun getCharacters(profile: RPKProfile): List<RPKCharacter> {
        return plugin.core.database.getTable(RPKCharacterTable::class).get(profile)
    }

    override fun getCharacters(name: String): List<RPKCharacter> {
        return plugin.core.database.getTable(RPKCharacterTable::class).get(name)
    }

    override fun addCharacter(character: RPKCharacter) {
        plugin.core.database.getTable(RPKCharacterTable::class).insert(character)
    }

    override fun removeCharacter(character: RPKCharacter) {
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            if (getActiveCharacter(minecraftProfile) == character) {
                setActiveCharacter(minecraftProfile, null)
            }
        }
        plugin.core.database.getTable(RPKCharacterTable::class).delete(character)
    }

    override fun updateCharacter(character: RPKCharacter) {
        if (plugin.config.getBoolean("characters.delete-character-on-death") && character.isDead) {
            removeCharacter(character)
        } else {
            plugin.core.database.getTable(RPKCharacterTable::class).update(character)
        }
    }

}
