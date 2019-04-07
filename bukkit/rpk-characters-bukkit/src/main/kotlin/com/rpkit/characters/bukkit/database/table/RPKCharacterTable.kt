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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterImpl
import com.rpkit.characters.bukkit.database.jooq.rpkit.Tables.PLAYER_CHARACTER
import com.rpkit.characters.bukkit.database.jooq.rpkit.Tables.RPKIT_CHARACTER
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import com.rpkit.core.bukkit.util.itemStackArrayFromByteArray
import com.rpkit.core.bukkit.util.itemStackFromByteArray
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType

/**
 * Represents the character table.
 */
class RPKCharacterTable(database: Database, private val plugin: RPKCharactersBukkit): Table<RPKCharacter>(database, RPKCharacter::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character.id.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_character.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacter::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create.createTableIfNotExists(RPKIT_CHARACTER)
                    .column(RPKIT_CHARACTER.ID, SQLDataType.INTEGER.identity(true))
                    .column(RPKIT_CHARACTER.PLAYER_ID, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.PROFILE_ID, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.NAME, SQLDataType.VARCHAR(256))
                    .column(RPKIT_CHARACTER.GENDER_ID, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.AGE, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.RACE_ID, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.DESCRIPTION, SQLDataType.VARCHAR(1024))
                    .column(RPKIT_CHARACTER.DEAD, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.WORLD, SQLDataType.VARCHAR(256))
                    .column(RPKIT_CHARACTER.X, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.Y, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.Z, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.YAW, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.PITCH, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.INVENTORY_CONTENTS, SQLDataType.BLOB)
                    .column(RPKIT_CHARACTER.HELMET, SQLDataType.BLOB)
                    .column(RPKIT_CHARACTER.CHESTPLATE, SQLDataType.BLOB)
                    .column(RPKIT_CHARACTER.LEGGINGS, SQLDataType.BLOB)
                    .column(RPKIT_CHARACTER.BOOTS, SQLDataType.BLOB)
                    .column(RPKIT_CHARACTER.HEALTH, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.MAX_HEALTH, SQLDataType.DOUBLE)
                    .column(RPKIT_CHARACTER.MANA, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.MAX_MANA, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.FOOD_LEVEL, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.THIRST_LEVEL, SQLDataType.INTEGER)
                    .column(RPKIT_CHARACTER.PLAYER_HIDDEN, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.PROFILE_HIDDEN, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.NAME_HIDDEN, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.GENDER_HIDDEN, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.AGE_HIDDEN, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.RACE_HIDDEN, SQLDataType.TINYINT.length(1))
                    .column(RPKIT_CHARACTER.DESCRIPTION_HIDDEN, SQLDataType.TINYINT.length(1))
                    .constraints(
                            constraint("pk_rpkit_character").primaryKey(RPKIT_CHARACTER.ID)
                    )
                    .execute()
        database.create
                .createTableIfNotExists(PLAYER_CHARACTER)
                .column(PLAYER_CHARACTER.PLAYER_ID, SQLDataType.INTEGER)
                .column(PLAYER_CHARACTER.CHARACTER_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("uk_player_character").unique(PLAYER_CHARACTER.PLAYER_ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.1.0") {
            database.setTableVersion(this, "0.1.1")
        }
        if (database.getTableVersion(this) == "0.1.1") {
            database.setTableVersion(this, "0.1.2")
        }
        if (database.getTableVersion(this) == "0.1.2") {
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.PLAYER_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.NAME_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.GENDER_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.AGE_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.RACE_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.DESCRIPTION_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.setTableVersion(this, "0.4.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.create.alterTable(RPKIT_CHARACTER)
                    .addColumn(RPKIT_CHARACTER.PROFILE_HIDDEN, SQLDataType.TINYINT.length(1))
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKCharacter): Int {
        database.create
                .insertInto(
                        RPKIT_CHARACTER,
                        RPKIT_CHARACTER.PLAYER_ID,
                        RPKIT_CHARACTER.PROFILE_ID,
                        RPKIT_CHARACTER.MINECRAFT_PROFILE_ID,
                        RPKIT_CHARACTER.NAME,
                        RPKIT_CHARACTER.GENDER_ID,
                        RPKIT_CHARACTER.AGE,
                        RPKIT_CHARACTER.RACE_ID,
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
                        RPKIT_CHARACTER.PLAYER_HIDDEN,
                        RPKIT_CHARACTER.PROFILE_HIDDEN,
                        RPKIT_CHARACTER.NAME_HIDDEN,
                        RPKIT_CHARACTER.GENDER_HIDDEN,
                        RPKIT_CHARACTER.AGE_HIDDEN,
                        RPKIT_CHARACTER.RACE_HIDDEN,
                        RPKIT_CHARACTER.DESCRIPTION_HIDDEN
                )
                .values(
                        entity.player?.id,
                        entity.profile?.id,
                        entity.minecraftProfile?.id,
                        entity.name,
                        entity.gender?.id,
                        entity.age,
                        entity.race?.id,
                        entity.description,
                        entity.isDead,
                        entity.location.world?.name,
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
                        entity.isPlayerHidden,
                        entity.isProfileHidden,
                        entity.isNameHidden,
                        entity.isGenderHidden,
                        entity.isAgeHidden,
                        entity.isRaceHidden,
                        entity.isDescriptionHidden
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKCharacter) {
        database.create
                .update(RPKIT_CHARACTER)
                .set(RPKIT_CHARACTER.PLAYER_ID, entity.player?.id)
                .set(RPKIT_CHARACTER.PROFILE_ID, entity.profile?.id)
                .set(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID, entity.minecraftProfile?.id)
                .set(RPKIT_CHARACTER.NAME, entity.name)
                .set(RPKIT_CHARACTER.GENDER_ID, entity.gender?.id)
                .set(RPKIT_CHARACTER.AGE, entity.age)
                .set(RPKIT_CHARACTER.RACE_ID, entity.race?.id)
                .set(RPKIT_CHARACTER.DESCRIPTION, entity.description)
                .set(RPKIT_CHARACTER.DEAD, if (entity.isDead) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.WORLD, entity.location.world?.name)
                .set(RPKIT_CHARACTER.X, entity.location.x)
                .set(RPKIT_CHARACTER.Y, entity.location.y)
                .set(RPKIT_CHARACTER.Z, entity.location.z)
                .set(RPKIT_CHARACTER.YAW, entity.location.yaw.toDouble())
                .set(RPKIT_CHARACTER.PITCH, entity.location.pitch.toDouble())
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
                .set(RPKIT_CHARACTER.PLAYER_HIDDEN, if (entity.isPlayerHidden) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.PROFILE_HIDDEN, if (entity.isProfileHidden) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.NAME_HIDDEN, if (entity.isNameHidden) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.GENDER_HIDDEN, if (entity.isGenderHidden) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.AGE_HIDDEN, if (entity.isAgeHidden) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.RACE_HIDDEN, if (entity.isRaceHidden) 1.toByte() else 0.toByte())
                .set(RPKIT_CHARACTER.DESCRIPTION_HIDDEN, if (entity.isDescriptionHidden) 1.toByte() else 0.toByte())
                .where(RPKIT_CHARACTER.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKCharacter? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                        RPKIT_CHARACTER.ID,
                        RPKIT_CHARACTER.PLAYER_ID,
                        RPKIT_CHARACTER.PROFILE_ID,
                        RPKIT_CHARACTER.MINECRAFT_PROFILE_ID,
                        RPKIT_CHARACTER.NAME,
                        RPKIT_CHARACTER.GENDER_ID,
                        RPKIT_CHARACTER.AGE,
                        RPKIT_CHARACTER.RACE_ID,
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
                        RPKIT_CHARACTER.PLAYER_HIDDEN,
                        RPKIT_CHARACTER.PROFILE_HIDDEN,
                        RPKIT_CHARACTER.NAME_HIDDEN,
                        RPKIT_CHARACTER.GENDER_HIDDEN,
                        RPKIT_CHARACTER.AGE_HIDDEN,
                        RPKIT_CHARACTER.RACE_HIDDEN,
                        RPKIT_CHARACTER.DESCRIPTION_HIDDEN
                )
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.ID.eq(id))
                .fetchOne() ?: return null

            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
            val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
            val playerId = result.get(RPKIT_CHARACTER.PLAYER_ID)
            val player = if (playerId == null) null else playerProvider.getPlayer(playerId)
            val profileId = result.get(RPKIT_CHARACTER.PROFILE_ID)
            val profile = if (profileId == null) null else profileProvider.getProfile(profileId)
            val minecraftProfileId = result.get(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID)
            val minecraftProfile = if (minecraftProfileId == null) null else minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            val genderId = result.get(RPKIT_CHARACTER.GENDER_ID)
            val gender = if (genderId == null) null else genderProvider.getGender(genderId)
            val raceId = result.get(RPKIT_CHARACTER.RACE_ID)
            val race = if (raceId == null) null else raceProvider.getRace(raceId)
            val helmetBytes = result.get(RPKIT_CHARACTER.HELMET)
            val helmet = if (helmetBytes == null) null else itemStackFromByteArray(helmetBytes)
            val chestplateBytes = result.get(RPKIT_CHARACTER.CHESTPLATE)
            val chestplate = if (chestplateBytes == null) null else itemStackFromByteArray(chestplateBytes)
            val leggingsBytes = result.get(RPKIT_CHARACTER.LEGGINGS)
            val leggings = if (leggingsBytes == null) null else itemStackFromByteArray(leggingsBytes)
            val bootsBytes = result.get(RPKIT_CHARACTER.BOOTS)
            val boots = if (bootsBytes == null) null else itemStackFromByteArray(bootsBytes)
            val character = RPKCharacterImpl(
                    plugin = plugin,
                    id = result.get(RPKIT_CHARACTER.ID),
                    player = player,
                    profile = profile,
                    minecraftProfile = minecraftProfile,
                    name = result.get(RPKIT_CHARACTER.NAME),
                    gender = gender,
                    age = result.get(RPKIT_CHARACTER.AGE),
                    race = race,
                    description = result.get(RPKIT_CHARACTER.DESCRIPTION),
                    dead = result.get(RPKIT_CHARACTER.DEAD) == 1.toByte(),
                    location = Location(
                            plugin.server.getWorld(result.get(RPKIT_CHARACTER.WORLD)),
                            result.get(RPKIT_CHARACTER.X),
                            result.get(RPKIT_CHARACTER.Y),
                            result.get(RPKIT_CHARACTER.Z),
                            result.get(RPKIT_CHARACTER.YAW).toFloat(),
                            result.get(RPKIT_CHARACTER.PITCH).toFloat()
                    ),
                    inventoryContents = itemStackArrayFromByteArray(result.get(RPKIT_CHARACTER.INVENTORY_CONTENTS)),
                    helmet = helmet,
                    chestplate = chestplate,
                    leggings = leggings,
                    boots = boots,
                    health = result.get(RPKIT_CHARACTER.HEALTH),
                    maxHealth = result.get(RPKIT_CHARACTER.MAX_HEALTH),
                    mana = result.get(RPKIT_CHARACTER.MANA),
                    maxMana = result.get(RPKIT_CHARACTER.MAX_MANA),
                    foodLevel = result.get(RPKIT_CHARACTER.FOOD_LEVEL),
                    thirstLevel = result.get(RPKIT_CHARACTER.THIRST_LEVEL),
                    isPlayerHidden = result.get(RPKIT_CHARACTER.PLAYER_HIDDEN) == 1.toByte(),
                    isProfileHidden = result.get(RPKIT_CHARACTER.PROFILE_HIDDEN) == 1.toByte(),
                    isNameHidden = result.get(RPKIT_CHARACTER.NAME_HIDDEN) == 1.toByte(),
                    isGenderHidden = result.get(RPKIT_CHARACTER.GENDER_HIDDEN) == 1.toByte(),
                    isAgeHidden = result.get(RPKIT_CHARACTER.AGE_HIDDEN) == 1.toByte(),
                    isRaceHidden = result.get(RPKIT_CHARACTER.RACE_HIDDEN) == 1.toByte(),
                    isDescriptionHidden = result.get(RPKIT_CHARACTER.DESCRIPTION_HIDDEN) == 1.toByte()
            )
            cache?.put(id, character)
            return character
        }
    }

    fun getActive(player: RPKPlayer): RPKCharacter? {
        val result = database.create
                .select(PLAYER_CHARACTER.CHARACTER_ID)
                .from(PLAYER_CHARACTER)
                .where(PLAYER_CHARACTER.PLAYER_ID.eq(player.id))
                .fetchOne() ?: return null
        return get(result.get(PLAYER_CHARACTER.CHARACTER_ID))
    }

    fun get(player: RPKPlayer): List<RPKCharacter> {
        val results = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.PLAYER_ID.eq(player.id))
                .fetch()
        return results.map { result -> get(result.get(RPKIT_CHARACTER.ID)) }
                .filterNotNull()
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKCharacter? {
        val result = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_CHARACTER.ID))
    }

    fun get(profile: RPKProfile): List<RPKCharacter> {
        val results = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.PROFILE_ID.eq(profile.id))
                .fetch()
        return results.map { result -> get(result.get(RPKIT_CHARACTER.ID)) }
                .filterNotNull()
    }

    fun get(name: String): List<RPKCharacter> {
        val results = database.create
                .select(RPKIT_CHARACTER.ID)
                .from(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.NAME.likeIgnoreCase("%$name%"))
                .fetch()
        return results.map { result -> get(result.get(RPKIT_CHARACTER.ID)) }
                .filterNotNull()
    }

    override fun delete(entity: RPKCharacter) {
        database.create
                .deleteFrom(RPKIT_CHARACTER)
                .where(RPKIT_CHARACTER.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}
