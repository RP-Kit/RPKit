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
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterCreateEvent
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterDeleteEvent
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterUpdateEvent
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
        return characterTable.getActive(player)
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
        var oldCharacter = getActiveCharacter(minecraftProfile)
        val event = RPKBukkitCharacterSwitchEvent(minecraftProfile, oldCharacter, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        oldCharacter = event.fromCharacter
        val newCharacter = event.character
        if (oldCharacter != null) {
            oldCharacter.minecraftProfile = null
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitPlayer = offlineBukkitPlayer.player
            if (bukkitPlayer != null) {
                oldCharacter.inventoryContents = bukkitPlayer.inventory.contents
                oldCharacter.helmet = bukkitPlayer.inventory.helmet
                oldCharacter.chestplate = bukkitPlayer.inventory.chestplate
                oldCharacter.leggings = bukkitPlayer.inventory.leggings
                oldCharacter.boots = bukkitPlayer.inventory.boots
                oldCharacter.location = bukkitPlayer.location
                oldCharacter.health = bukkitPlayer.health
                oldCharacter.foodLevel = bukkitPlayer.foodLevel
            }
            updateCharacter(oldCharacter)
        }
        if (newCharacter != null) {
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitPlayer = offlineBukkitPlayer.player
            if (bukkitPlayer != null) {
                bukkitPlayer.inventory.contents = newCharacter.inventoryContents
                bukkitPlayer.inventory.helmet = newCharacter.helmet
                bukkitPlayer.inventory.chestplate = newCharacter.chestplate
                bukkitPlayer.inventory.leggings = newCharacter.leggings
                bukkitPlayer.inventory.boots = newCharacter.boots
                bukkitPlayer.teleport(newCharacter.location)
                bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = newCharacter.maxHealth
                bukkitPlayer.health = newCharacter.health
                bukkitPlayer.foodLevel = newCharacter.foodLevel
                if (plugin.config.getBoolean("characters.set-player-display-name")) {
                    bukkitPlayer.setDisplayName(newCharacter.name)
                }
            }
            newCharacter.minecraftProfile = minecraftProfile
            updateCharacter(newCharacter)
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
        return characterTable.get(player)
    }

    override fun getCharacters(profile: RPKProfile): List<RPKCharacter> {
        return plugin.core.database.getTable(RPKCharacterTable::class).get(profile)
    }

    override fun getCharacters(name: String): List<RPKCharacter> {
        return plugin.core.database.getTable(RPKCharacterTable::class).get(name)
    }

    override fun addCharacter(character: RPKCharacter) {
        val event = RPKBukkitCharacterCreateEvent(character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKCharacterTable::class).insert(event.character)
    }

    override fun removeCharacter(character: RPKCharacter) {
        val event = RPKBukkitCharacterDeleteEvent(character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val minecraftProfile = event.character.minecraftProfile
        if (minecraftProfile != null) {
            if (getActiveCharacter(minecraftProfile) == event.character) {
                setActiveCharacter(minecraftProfile, null)
            }
        }
        plugin.core.database.getTable(RPKCharacterTable::class).delete(event.character)
    }

    override fun updateCharacter(character: RPKCharacter) {
        if (plugin.config.getBoolean("characters.delete-character-on-death") && character.isDead) {
            removeCharacter(character)
        } else {
            val event = RPKBukkitCharacterUpdateEvent(character)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.core.database.getTable(RPKCharacterTable::class).update(event.character)
        }
    }

}
