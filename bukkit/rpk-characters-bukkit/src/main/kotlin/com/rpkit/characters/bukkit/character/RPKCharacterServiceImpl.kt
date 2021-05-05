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
import com.rpkit.characters.bukkit.race.RPKRaceName
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Character service implementation.
 */
class RPKCharacterServiceImpl(override val plugin: RPKCharactersBukkit) : RPKCharacterService {

    private val activeCharacters = ConcurrentHashMap<Int, Int>()
    private val characters = ConcurrentHashMap<Int, RPKCharacter>()

    override fun getPreloadedCharacter(id: RPKCharacterId): RPKCharacter? {
        return characters[id.value]
    }

    override fun loadCharacter(id: RPKCharacterId): CompletableFuture<RPKCharacter?> {
        val preloadedCharacter = getPreloadedCharacter(id)
        if (preloadedCharacter != null) return CompletableFuture.completedFuture(preloadedCharacter)
        plugin.logger.info("Loading character ${id.value}...")
        val characterFuture = plugin.database.getTable(RPKCharacterTable::class.java)[id]
        characterFuture.thenAccept { character ->
            if (character != null) {
                characters[id.value] = character
                plugin.logger.info("Loaded character ${character.name} (${id.value})")
            }
        }
        return characterFuture
    }

    override fun unloadCharacter(id: RPKCharacterId) {
        characters.remove(id.value)
        plugin.logger.info("Unloaded character ${id.value}")
    }

    override fun getCharacter(id: RPKCharacterId): CompletableFuture<RPKCharacter?> {
        val preloaded = getPreloadedCharacter(id)
        if (preloaded != null) return CompletableFuture.completedFuture(preloaded)
        return plugin.database.getTable(RPKCharacterTable::class.java)[id]
    }

    override fun getActiveCharacter(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKCharacter?> {
        val preloaded = getPreloadedActiveCharacter(minecraftProfile)
        if (preloaded != null) return CompletableFuture.completedFuture(preloaded)
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return plugin.database.getTable(RPKCharacterTable::class.java).get(minecraftProfileId)
    }

    override fun getPreloadedActiveCharacter(minecraftProfile: RPKMinecraftProfile): RPKCharacter? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        val activeCharacterId = activeCharacters[minecraftProfileId.value] ?: return null
        return characters[activeCharacterId]
    }

    override fun loadActiveCharacter(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKCharacter?> {
        val preloadedActiveCharacter = getPreloadedActiveCharacter(minecraftProfile)
        if (preloadedActiveCharacter != null) return CompletableFuture.completedFuture(preloadedActiveCharacter)
        plugin.logger.info("Loading active character for Minecraft user ${minecraftProfile.minecraftUsername.value} (${minecraftProfile.minecraftUUID})...")
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        val characterFuture = plugin.database.getTable(RPKCharacterTable::class.java).get(minecraftProfileId)
        characterFuture.thenAccept { character ->
            val characterId = character?.id
            if (characterId != null) {
                characters[characterId.value] = character
                plugin.logger.info("Loaded character ${character.name} (${characterId.value})")
                activeCharacters[minecraftProfileId.value] = characterId.value
                plugin.logger.info("Character ${character.name} (${characterId.value}) loaded for Minecraft user ${minecraftProfile.minecraftUsername.value} (${minecraftProfile.minecraftUUID})")
            }
        }
        return characterFuture
    }

    override fun unloadActiveCharacter(minecraftProfile: RPKMinecraftProfile) {
        val minecraftProfileId = minecraftProfile.id ?: return
        val activeCharacterId = activeCharacters[minecraftProfileId.value] ?: return
        characters.remove(activeCharacterId)
        activeCharacters.remove(minecraftProfileId.value)
        plugin.logger.info("Unloaded active character for Minecraft user ${minecraftProfile.minecraftUsername.value} (${minecraftProfile.minecraftUUID})")
    }

    override fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: RPKCharacter?): CompletableFuture<Void> {
        return getActiveCharacter(minecraftProfile).thenAccept { oldCharacter ->
            plugin.server.scheduler.runTask(plugin, Runnable {
                val event = RPKBukkitCharacterSwitchEvent(minecraftProfile, oldCharacter, character, !plugin.server.isPrimaryThread)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@Runnable
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
                        if (oldCharacter.isDead) {
                            bukkitPlayer.removePotionEffect(PotionEffectType.BLINDNESS)
                            bukkitPlayer.removePotionEffect(PotionEffectType.SLOW)
                        }
                    }
                    updateCharacter(oldCharacter)
                    val minecraftProfileId = minecraftProfile.id
                    if (minecraftProfileId != null) {
                        activeCharacters.remove(minecraftProfileId.value)
                    }
                    oldCharacter.id?.let(::unloadCharacter)
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
                        if (newCharacter.isDead) {
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000, 0))
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 1000000, 255))
                        }
                    }
                    newCharacter.minecraftProfile = minecraftProfile
                    updateCharacter(newCharacter)
                    val minecraftProfileId = minecraftProfile.id
                    val characterId = newCharacter.id
                    if (characterId != null) {
                        characters[characterId.value] = newCharacter
                        plugin.logger.info("Loaded character ${newCharacter.name} (${characterId.value})")
                        if (minecraftProfileId != null) {
                            activeCharacters[minecraftProfileId.value] = characterId.value
                            plugin.logger.info("Character ${newCharacter.name} (${characterId.value}) loaded for Minecraft user ${minecraftProfile.minecraftUsername.value} (${minecraftProfile.minecraftUUID})")
                        }
                    }
                }
            })
        }
    }

    override fun getCharacters(profile: RPKProfile): CompletableFuture<List<RPKCharacter>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return plugin.database.getTable(RPKCharacterTable::class.java).get(profileId)
    }

    override fun addCharacter(character: RPKCharacter): CompletableFuture<Void> {
        val event = RPKBukkitCharacterCreateEvent(character, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(null)
        return plugin.database.getTable(RPKCharacterTable::class.java).insert(event.character)
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
    ): CompletableFuture<RPKCharacter> {
        val character = RPKCharacterImpl(
            plugin,
            null,
            profile,
            null,
            name ?: plugin.config.getString("characters.defaults.name") ?: "",
            gender ?: plugin.config.getString("characters.defaults.gender"),
            age ?: plugin.config.getInt("characters.defaults.age"),
            race ?: plugin.config.getString("characters.defaults.race")
                ?.let { Services[RPKRaceService::class.java]?.getRace(RPKRaceName(it)) },
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
        return addCharacter(character).thenApply { character }
    }

    override fun removeCharacter(character: RPKCharacter): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitCharacterDeleteEvent(character, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val minecraftProfile = event.character.minecraftProfile
            if (minecraftProfile != null) {
                if (getActiveCharacter(minecraftProfile).join() == event.character) {
                    setActiveCharacter(minecraftProfile, null).join()
                }
            }
            plugin.database.getTable(RPKCharacterTable::class.java).delete(event.character).join()
        }
    }

    override fun updateCharacter(character: RPKCharacter): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (plugin.config.getBoolean("characters.delete-character-on-death") && character.isDead) {
                removeCharacter(character).join()
            } else {
                val event = RPKBukkitCharacterUpdateEvent(character, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@runAsync
                plugin.database.getTable(RPKCharacterTable::class.java).update(event.character).join()
            }
        }
    }

}
