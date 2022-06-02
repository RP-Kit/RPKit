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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterImpl
import com.rpkit.characters.bukkit.database.create
import com.rpkit.characters.bukkit.database.jooq.Tables.RPKIT_CHARACTER
import com.rpkit.characters.bukkit.race.RPKRaceName
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.core.bukkit.extension.toByteArray
import com.rpkit.core.bukkit.extension.toItemStack
import com.rpkit.core.bukkit.extension.toItemStackArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Represents the character table.
 */
class RPKCharacterTable(private val database: Database, private val plugin: RPKCharactersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-characters-bukkit.rpkit_character.id",
            Int::class.javaObjectType,
            RPKCharacter::class.java,
            plugin.config.getLong("caching.rpkit_character.id.size")
        )
    } else {
        null
    }

    private val minecraftProfileIdCache = if (plugin.config.getBoolean("caching.rpkit_character.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-characters-bukkit.rpkit_character.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKCharacter::class.java,
            plugin.config.getLong("caching.rpkit_character.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacter): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CHARACTER,
                    RPKIT_CHARACTER.PROFILE_ID,
                    RPKIT_CHARACTER.MINECRAFT_PROFILE_ID,
                    RPKIT_CHARACTER.NAME,
                    RPKIT_CHARACTER.GENDER,
                    RPKIT_CHARACTER.AGE,
                    RPKIT_CHARACTER.RACE_NAME,
                    RPKIT_CHARACTER.DESCRIPTION,
                    RPKIT_CHARACTER.DEAD,
                    RPKIT_CHARACTER.WORLD,
                    RPKIT_CHARACTER.X,
                    RPKIT_CHARACTER.Y,
                    RPKIT_CHARACTER.Z,
                    RPKIT_CHARACTER.YAW,
                    RPKIT_CHARACTER.PITCH,
                    RPKIT_CHARACTER.INVENTORY_CONTENTS,
                    RPKIT_CHARACTER.HELMET,
                    RPKIT_CHARACTER.CHESTPLATE,
                    RPKIT_CHARACTER.LEGGINGS,
                    RPKIT_CHARACTER.BOOTS,
                    RPKIT_CHARACTER.HEALTH,
                    RPKIT_CHARACTER.MAX_HEALTH,
                    RPKIT_CHARACTER.MANA,
                    RPKIT_CHARACTER.MAX_MANA,
                    RPKIT_CHARACTER.FOOD_LEVEL,
                    RPKIT_CHARACTER.THIRST_LEVEL,
                    RPKIT_CHARACTER.PROFILE_HIDDEN,
                    RPKIT_CHARACTER.NAME_HIDDEN,
                    RPKIT_CHARACTER.GENDER_HIDDEN,
                    RPKIT_CHARACTER.AGE_HIDDEN,
                    RPKIT_CHARACTER.RACE_HIDDEN,
                    RPKIT_CHARACTER.DESCRIPTION_HIDDEN
                )
                .values(
                    entity.profile?.id?.value,
                    entity.minecraftProfile?.id?.value,
                    entity.name,
                    entity.gender,
                    entity.age,
                    entity.race?.name?.value,
                    entity.description,
                    entity.isDead,
                    entity.location.world,
                    entity.location.x,
                    entity.location.y,
                    entity.location.z,
                    entity.location.yaw,
                    entity.location.pitch,
                    entity.inventoryContents.toByteArray(),
                    entity.helmet?.toByteArray(),
                    entity.chestplate?.toByteArray(),
                    entity.leggings?.toByteArray(),
                    entity.boots?.toByteArray(),
                    entity.health,
                    entity.maxHealth,
                    entity.mana,
                    entity.maxMana,
                    entity.foodLevel,
                    entity.thirstLevel,
                    entity.isProfileHidden,
                    entity.isNameHidden,
                    entity.isGenderHidden,
                    entity.isAgeHidden,
                    entity.isRaceHidden,
                    entity.isDescriptionHidden
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKCharacterId(id)
            cache?.set(id, entity)
            val minecraftProfileId = entity.minecraftProfile?.id
            if (minecraftProfileId != null) {
                minecraftProfileIdCache?.set(minecraftProfileId.value, entity)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert character", exception)
            throw exception
        }
    }

    fun update(entity: RPKCharacter): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_CHARACTER)
                .set(RPKIT_CHARACTER.PROFILE_ID, entity.profile?.id?.value)
                .set(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID, entity.minecraftProfile?.id?.value)
                .set(RPKIT_CHARACTER.NAME, entity.name)
                .set(RPKIT_CHARACTER.GENDER, entity.gender)
                .set(RPKIT_CHARACTER.AGE, entity.age)
                .set(RPKIT_CHARACTER.RACE_NAME, entity.race?.name?.value)
                .set(RPKIT_CHARACTER.DESCRIPTION, entity.description)
                .set(RPKIT_CHARACTER.DEAD, entity.isDead)
                .set(RPKIT_CHARACTER.WORLD, entity.location.world)
                .set(RPKIT_CHARACTER.X, entity.location.x)
                .set(RPKIT_CHARACTER.Y, entity.location.y)
                .set(RPKIT_CHARACTER.Z, entity.location.z)
                .set(RPKIT_CHARACTER.YAW, entity.location.yaw)
                .set(RPKIT_CHARACTER.PITCH, entity.location.pitch)
                .set(RPKIT_CHARACTER.INVENTORY_CONTENTS, entity.inventoryContents.toByteArray())
                .set(RPKIT_CHARACTER.HELMET, entity.helmet?.toByteArray())
                .set(RPKIT_CHARACTER.CHESTPLATE, entity.chestplate?.toByteArray())
                .set(RPKIT_CHARACTER.LEGGINGS, entity.leggings?.toByteArray())
                .set(RPKIT_CHARACTER.BOOTS, entity.boots?.toByteArray())
                .set(RPKIT_CHARACTER.HEALTH, entity.health)
                .set(RPKIT_CHARACTER.MAX_HEALTH, entity.maxHealth)
                .set(RPKIT_CHARACTER.MANA, entity.mana)
                .set(RPKIT_CHARACTER.MAX_MANA, entity.maxMana)
                .set(RPKIT_CHARACTER.FOOD_LEVEL, entity.foodLevel)
                .set(RPKIT_CHARACTER.THIRST_LEVEL, entity.thirstLevel)
                .set(RPKIT_CHARACTER.PROFILE_HIDDEN, entity.isProfileHidden)
                .set(RPKIT_CHARACTER.NAME_HIDDEN, entity.isNameHidden)
                .set(RPKIT_CHARACTER.GENDER_HIDDEN, entity.isGenderHidden)
                .set(RPKIT_CHARACTER.AGE_HIDDEN, entity.isAgeHidden)
                .set(RPKIT_CHARACTER.RACE_HIDDEN, entity.isRaceHidden)
                .set(RPKIT_CHARACTER.DESCRIPTION_HIDDEN, entity.isDescriptionHidden)
                .where(RPKIT_CHARACTER.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
            val minecraftProfileId = entity.minecraftProfile?.id
            if (minecraftProfileId != null) {
                minecraftProfileIdCache?.set(minecraftProfileId.value, entity)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update character", exception)
            throw exception
        }
    }

    operator fun get(id: RPKCharacterId, overrideCache: Boolean = false): CompletableFuture<out RPKCharacter?> {
        if (!overrideCache && cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_CHARACTER.ID,
                        RPKIT_CHARACTER.PROFILE_ID,
                        RPKIT_CHARACTER.MINECRAFT_PROFILE_ID,
                        RPKIT_CHARACTER.NAME,
                        RPKIT_CHARACTER.GENDER,
                        RPKIT_CHARACTER.AGE,
                        RPKIT_CHARACTER.RACE_NAME,
                        RPKIT_CHARACTER.DESCRIPTION,
                        RPKIT_CHARACTER.DEAD,
                        RPKIT_CHARACTER.WORLD,
                        RPKIT_CHARACTER.X,
                        RPKIT_CHARACTER.Y,
                        RPKIT_CHARACTER.Z,
                        RPKIT_CHARACTER.YAW,
                        RPKIT_CHARACTER.PITCH,
                        RPKIT_CHARACTER.INVENTORY_CONTENTS,
                        RPKIT_CHARACTER.HELMET,
                        RPKIT_CHARACTER.CHESTPLATE,
                        RPKIT_CHARACTER.LEGGINGS,
                        RPKIT_CHARACTER.BOOTS,
                        RPKIT_CHARACTER.HEALTH,
                        RPKIT_CHARACTER.MAX_HEALTH,
                        RPKIT_CHARACTER.MANA,
                        RPKIT_CHARACTER.MAX_MANA,
                        RPKIT_CHARACTER.FOOD_LEVEL,
                        RPKIT_CHARACTER.THIRST_LEVEL,
                        RPKIT_CHARACTER.PROFILE_HIDDEN,
                        RPKIT_CHARACTER.NAME_HIDDEN,
                        RPKIT_CHARACTER.GENDER_HIDDEN,
                        RPKIT_CHARACTER.AGE_HIDDEN,
                        RPKIT_CHARACTER.RACE_HIDDEN,
                        RPKIT_CHARACTER.DESCRIPTION_HIDDEN
                    )
                    .from(RPKIT_CHARACTER)
                    .where(RPKIT_CHARACTER.ID.eq(id.value))
                    .fetchOne() ?: return@supplyAsync null

                val profileService = Services[RPKProfileService::class.java]
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                val raceService = Services[RPKRaceService::class.java]
                val profileId = result[RPKIT_CHARACTER.PROFILE_ID]
                val profile = if (profileId == null) null else profileService?.getProfile(RPKProfileId(profileId))?.join()
                val minecraftProfileId = result[RPKIT_CHARACTER.MINECRAFT_PROFILE_ID]
                val minecraftProfile = if (minecraftProfileId == null) {
                    null
                } else {
                    minecraftProfileService?.getMinecraftProfile(
                        RPKMinecraftProfileId(minecraftProfileId)
                    )?.join()
                }
                val raceName = result[RPKIT_CHARACTER.RACE_NAME]
                val race = if (raceName == null) null else raceService?.getRace(RPKRaceName(raceName))
                val character = RPKCharacterImpl(
                    plugin = plugin,
                    id = RPKCharacterId(result[RPKIT_CHARACTER.ID]),
                    profile = profile,
                    minecraftProfile = minecraftProfile,
                    name = result[RPKIT_CHARACTER.NAME],
                    gender = result[RPKIT_CHARACTER.GENDER],
                    age = result[RPKIT_CHARACTER.AGE],
                    race = race,
                    description = result[RPKIT_CHARACTER.DESCRIPTION],
                    dead = result[RPKIT_CHARACTER.DEAD],
                    location = RPKLocation(
                        result[RPKIT_CHARACTER.WORLD],
                        result[RPKIT_CHARACTER.X],
                        result[RPKIT_CHARACTER.Y],
                        result[RPKIT_CHARACTER.Z],
                        result[RPKIT_CHARACTER.YAW].toFloat(),
                        result[RPKIT_CHARACTER.PITCH].toFloat()
                    ),
                    inventoryContents = result[RPKIT_CHARACTER.INVENTORY_CONTENTS]?.toItemStackArray() ?: emptyArray(),
                    helmet = result[RPKIT_CHARACTER.HELMET]?.toItemStack(),
                    chestplate = result[RPKIT_CHARACTER.CHESTPLATE]?.toItemStack(),
                    leggings = result[RPKIT_CHARACTER.LEGGINGS]?.toItemStack(),
                    boots = result[RPKIT_CHARACTER.BOOTS]?.toItemStack(),
                    health = result[RPKIT_CHARACTER.HEALTH],
                    maxHealth = result[RPKIT_CHARACTER.MAX_HEALTH],
                    mana = result[RPKIT_CHARACTER.MANA],
                    maxMana = result[RPKIT_CHARACTER.MAX_MANA],
                    foodLevel = result[RPKIT_CHARACTER.FOOD_LEVEL],
                    thirstLevel = result[RPKIT_CHARACTER.THIRST_LEVEL],
                    isProfileHidden = result[RPKIT_CHARACTER.PROFILE_HIDDEN],
                    isNameHidden = result[RPKIT_CHARACTER.NAME_HIDDEN],
                    isGenderHidden = result[RPKIT_CHARACTER.GENDER_HIDDEN],
                    isAgeHidden = result[RPKIT_CHARACTER.AGE_HIDDEN],
                    isRaceHidden = result[RPKIT_CHARACTER.RACE_HIDDEN],
                    isDescriptionHidden = result[RPKIT_CHARACTER.DESCRIPTION_HIDDEN]
                )
                cache?.set(id.value, character)
                return@supplyAsync character
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get character", exception)
                throw exception
            }
        }
    }

    fun get(minecraftProfileId: RPKMinecraftProfileId): CompletableFuture<RPKCharacter?> {
        if (minecraftProfileIdCache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(minecraftProfileIdCache[minecraftProfileId.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync get(RPKCharacterId(result[RPKIT_CHARACTER.ID])).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get active character", exception)
            throw exception
        }
    }

    fun get(profileId: RPKProfileId): CompletableFuture<List<RPKCharacter>> {
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.PROFILE_ID.eq(profileId.value))
                .fetch()
            val futures = results.map { result -> get(RPKCharacterId(result[RPKIT_CHARACTER.ID])) }
            CompletableFuture.allOf(*futures.toTypedArray()).join()
            return@supplyAsync futures.mapNotNull(CompletableFuture<out RPKCharacter?>::join)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get characters for profile", exception)
            throw exception
        }
    }

    fun get(name: String): CompletableFuture<List<RPKCharacter>> {
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.NAME.likeIgnoreCase("%$name%"))
                .fetch()
            val futures = results.map { result -> get(RPKCharacterId(result[RPKIT_CHARACTER.ID])) }
            CompletableFuture.allOf(*futures.toTypedArray()).join()
            return@supplyAsync futures.mapNotNull(CompletableFuture<out RPKCharacter?>::join)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get characters for name", exception)
            throw exception
        }
    }

    fun delete(entity: RPKCharacter): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
            val minecraftProfileId = entity.minecraftProfile?.id
            if (minecraftProfileId != null) {
                minecraftProfileIdCache?.remove(minecraftProfileId.value)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete character", exception)
            throw exception
        }
    }

}
