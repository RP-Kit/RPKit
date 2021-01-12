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

package com.rpkit.characters.bukkit.character

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.table.RPKCharacterTable
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterCreateEvent
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterDeleteEvent
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterUpdateEvent
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack

/**
 * Character service implementation.
 */
class RPKCharacterServiceImpl(override val plugin: RPKCharactersBukkit) : RPKCharacterService {

    override fun getCharacter(id: RPKCharacterId): RPKCharacter? {
        return plugin.database.getTable(RPKCharacterTable::class.java)[id]
    }

    override fun getActiveCharacter(minecraftProfile: RPKMinecraftProfile): RPKCharacter? {
        return plugin.database.getTable(RPKCharacterTable::class.java).get(minecraftProfile)
    }

    override fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: RPKCharacter?) {
        var oldCharacter = getActiveCharacter(minecraftProfile)
        val event = RPKBukkitCharacterSwitchEvent(minecraftProfile, oldCharacter, character, !plugin.server.isPrimaryThread)
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

    override fun getCharacters(profile: RPKProfile): List<RPKCharacter> {
        return plugin.database.getTable(RPKCharacterTable::class.java).get(profile)
    }

    override fun getCharacters(name: String): List<RPKCharacter> {
        return plugin.database.getTable(RPKCharacterTable::class.java).get(name)
    }

    override fun addCharacter(character: RPKCharacter) {
        val event = RPKBukkitCharacterCreateEvent(character, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKCharacterTable::class.java).insert(event.character)
    }

    override fun createCharacter(
        profile: RPKProfile?,
        name: String?,
        gender: String?,
        age: Int?,
        race: RPKRace?,
        description: String?,
        isDead: Boolean?,
        location: Location?,
        inventoryContents: Array<ItemStack>?,
        helmet: ItemStack?,
        chestplate: ItemStack?,
        leggings: ItemStack?,
        boots: ItemStack?,
        health: Double?,
        maxHealth: Double?,
        mana: Int?,
        maxMana: Int?,
        foodLevel: Int?,
        thirstLevel: Int?,
        isProfileHidden: Boolean?,
        isNameHidden: Boolean?,
        isGenderHidden: Boolean?,
        isAgeHidden: Boolean?,
        isRaceHidden: Boolean?,
        isDescriptionHidden: Boolean?
    ): RPKCharacter {
        val character = RPKCharacterImpl(
            plugin,
            null,
            profile,
            null,
            name ?: plugin.config.getString("characters.defaults.name") ?: "",
            gender ?: plugin.config.getString("characters.defaults.gender"),
            age ?: plugin.config.getInt("characters.defaults.age"),
            race ?: plugin.config.getString("characters.defaults.race")
                ?.let { Services[RPKRaceService::class.java]?.getRace(it) },
            description ?: plugin.config.getString("characters.defaults.description") ?: "",
            isDead ?: plugin.config.getBoolean("characters.defaults.dead"),
            location ?: plugin.server.worlds[0].spawnLocation,
            inventoryContents
                ?: (plugin.config.getList("characters.defaults.inventory-contents") as MutableList<ItemStack>)
                    .toTypedArray(),
            helmet ?: plugin.config.getItemStack("characters.defaults.helmet"),
            chestplate ?: plugin.config.getItemStack("characters.defaults.chestplate"),
            leggings ?: plugin.config.getItemStack("characters.defaults.leggings"),
            boots ?: plugin.config.getItemStack("characters.defaults.boots"),
            health ?: plugin.config.getDouble("characters.defaults.health"),
            maxHealth ?: plugin.config.getDouble("characters.defaults.max-health"),
            mana ?: plugin.config.getInt("characters.defaults.mana"),
            maxMana ?: plugin.config.getInt("characters.defaults.max-mana"),
            foodLevel ?: plugin.config.getInt("characters.defaults.food-level"),
            thirstLevel ?: plugin.config.getInt("characters.defaults.thirst-level"),
            isProfileHidden ?: plugin.config.getBoolean("characters.defaults.profile-hidden"),
            isNameHidden ?: plugin.config.getBoolean("characters.defaults.name-hidden"),
            isGenderHidden ?: plugin.config.getBoolean("characters.defaults.gender-hidden"),
            isAgeHidden ?: plugin.config.getBoolean("characters.defaults.age-hidden"),
            isRaceHidden ?: plugin.config.getBoolean("characters.defaults.race-hidden"),
            isDescriptionHidden ?: plugin.config.getBoolean("characters.defaults.description-hidden")
        )
        addCharacter(character)
        return character
    }

    override fun removeCharacter(character: RPKCharacter) {
        val event = RPKBukkitCharacterDeleteEvent(character, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val minecraftProfile = event.character.minecraftProfile
        if (minecraftProfile != null) {
            if (getActiveCharacter(minecraftProfile) == event.character) {
                setActiveCharacter(minecraftProfile, null)
            }
        }
        plugin.database.getTable(RPKCharacterTable::class.java).delete(event.character)
    }

    override fun updateCharacter(character: RPKCharacter) {
        if (plugin.config.getBoolean("characters.delete-character-on-death") && character.isDead) {
            removeCharacter(character)
        } else {
            val event = RPKBukkitCharacterUpdateEvent(character, !plugin.server.isPrimaryThread)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.database.getTable(RPKCharacterTable::class.java).update(event.character)
        }
    }

}
