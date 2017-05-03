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
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import com.rpkit.core.bukkit.util.itemStackArrayFromByteArray
import com.rpkit.core.bukkit.util.itemStackFromByteArray
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.Bukkit
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.BLOB
import java.sql.Types.INTEGER

/**
 * Represents the character table.
 */
class RPKCharacterTable: Table<RPKCharacter> {

    private val plugin: RPKCharactersBukkit
    private val cacheManager: org.ehcache.CacheManager
    private val cache: org.ehcache.Cache<Int, RPKCharacter>

    constructor(database: Database, plugin: RPKCharactersBukkit): super(database, RPKCharacter::class.java) {
        this.plugin = plugin
        cacheManager = org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacter::class.java,
                        ResourcePoolsBuilder.heap((plugin.server.maxPlayers * 2).toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_character (" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "profile_id INTEGER," +
                            "minecraft_profile_id INTEGER," +
                            "name VARCHAR(256)," +
                            "gender_id INTEGER," +
                            "age INTEGER," +
                            "race_id INTEGER," +
                            "description VARCHAR(1024)," +
                            "dead BOOLEAN," +
                            "world VARCHAR(256)," +
                            "x DOUBLE," +
                            "y DOUBLE," +
                            "z DOUBLE," +
                            "yaw REAL," +
                            "pitch REAL," +
                            "inventory_contents BLOB," +
                            "helmet BLOB," +
                            "chestplate BLOB," +
                            "leggings BLOB," +
                            "boots BLOB," +
                            "health DOUBLE," +
                            "max_health DOUBLE," +
                            "mana INTEGER," +
                            "max_mana INTEGER," +
                            "food_level INTEGER," +
                            "thirst_level INTEGER," +
                            "player_hidden BOOLEAN," +
                            "profile_hidden BOOLEAN," +
                            "name_hidden BOOLEAN," +
                            "gender_hidden BOOLEAN," +
                            "age_hidden BOOLEAN," +
                            "race_hidden BOOLEAN," +
                            "description_hidden BOOLEAN" +
                    ")").use(PreparedStatement::executeUpdate)
        }
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_character(" +
                            "player_id INTEGER," +
                            "character_id INTEGER," +
                            "UNIQUE(player_id)" +
                    ")").use(PreparedStatement::executeUpdate)
        }
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
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "ALTER TABLE rpkit_character " +
                                "ADD COLUMN player_hidden BOOLEAN," +
                                "ADD COLUMN name_hidden BOOLEAN," +
                                "ADD COLUMN gender_hidden BOOLEAN," +
                                "ADD COLUMN age_hidden BOOLEAN," +
                                "ADD COLUMN race_hidden BOOLEAN," +
                                "ADD COLUMN description_hidden BOOLEAN"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "0.4.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "ALTER TABLE rpkit_character " +
                                "ADD COLUMN profile_id INTEGER AFTER player_id," +
                                "ADD COLUMN minecraft_profile_id INTEGER AFTER profile_id," +
                                "ADD COLUMN profile_hidden BOOLEAN AFTER player_hidden"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKCharacter): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_character(player_id, profile_id, minecraft_profile_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level, player_hidden, profile_hidden, name_hidden, gender_hidden, age_hidden, race_hidden, description_hidden) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                val player = entity.player
                if (player != null) {
                    statement.setInt(1, player.id)
                } else {
                    statement.setNull(1, INTEGER)
                }
                val profile = entity.profile
                if (profile != null) {
                    statement.setInt(2, profile.id)
                } else {
                    statement.setNull(2, INTEGER)
                }
                val minecraftProfile = entity.minecraftProfile
                if (minecraftProfile != null) {
                    statement.setInt(3, minecraftProfile.id)
                } else {
                    statement.setInt(3, INTEGER)
                }
                statement.setString(4, entity.name)
                val gender = entity.gender
                if (gender != null) {
                    statement.setInt(5, gender.id)
                } else {
                    statement.setNull(5, INTEGER)
                }
                statement.setInt(6, entity.age)
                val race = entity.race
                if (race != null) {
                    statement.setInt(7, race.id)
                } else {
                    statement.setNull(7, INTEGER)
                }
                statement.setString(8, entity.description)
                statement.setBoolean(9, entity.isDead)
                statement.setString(10, entity.location.world.name)
                statement.setDouble(11, entity.location.x)
                statement.setDouble(12, entity.location.y)
                statement.setDouble(13, entity.location.z)
                statement.setFloat(14, entity.location.yaw)
                statement.setFloat(15, entity.location.pitch)
                statement.setBytes(16, entity.inventoryContents.toByteArray())
                val helmet = entity.helmet
                if (helmet != null) {
                    statement.setBytes(17, helmet.toByteArray())
                } else {
                    statement.setNull(17, BLOB)
                }
                val chestplate = entity.chestplate
                if (chestplate != null) {
                    statement.setBytes(18, chestplate.toByteArray())
                } else {
                    statement.setNull(18, BLOB)
                }
                val leggings = entity.leggings
                if (leggings != null) {
                    statement.setBytes(19, leggings.toByteArray())
                } else {
                    statement.setNull(19, BLOB)
                }
                val boots = entity.boots
                if (boots != null) {
                    statement.setBytes(20, boots.toByteArray())
                } else {
                    statement.setNull(20, BLOB)
                }
                statement.setDouble(21, entity.health)
                statement.setDouble(22, entity.maxHealth)
                statement.setInt(23, entity.mana)
                statement.setInt(24, entity.maxMana)
                statement.setInt(25, entity.foodLevel)
                statement.setInt(26, entity.thirstLevel)
                statement.setBoolean(27, entity.isPlayerHidden)
                statement.setBoolean(28, entity.isProfileHidden)
                statement.setBoolean(29, entity.isNameHidden)
                statement.setBoolean(30, entity.isGenderHidden)
                statement.setBoolean(31, entity.isAgeHidden)
                statement.setBoolean(32, entity.isRaceHidden)
                statement.setBoolean(33, entity.isDescriptionHidden)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKCharacter) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_character SET player_id = ?, profile_id = ?, minecraft_profile_id = ?, name = ?, gender_id = ?, age = ?, race_id = ?, description = ?, dead = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, inventory_contents = ?, helmet = ?, chestplate = ?, leggings = ?, boots = ?, health = ?, max_health = ?, mana = ?, max_mana = ?, food_level = ?, thirst_level = ?, player_hidden = ?, profile_hidden = ?, name_hidden = ?, gender_hidden = ?, age_hidden = ?, race_hidden = ?, description_hidden = ? WHERE id = ?").use { statement ->
                val player = entity.player
                if (player != null) {
                    statement.setInt(1, player.id)
                } else {
                    statement.setNull(1, INTEGER)
                }
                val profile = entity.profile
                if (profile != null) {
                    statement.setInt(2, profile.id)
                } else {
                    statement.setNull(2, INTEGER)
                }
                val minecraftProfile = entity.minecraftProfile
                if (minecraftProfile != null) {
                    statement.setInt(3, minecraftProfile.id)
                } else {
                    statement.setNull(3, INTEGER)
                }
                statement.setString(4, entity.name)
                val gender = entity.gender
                if (gender != null) {
                    statement.setInt(5, gender.id)
                } else {
                    statement.setNull(5, INTEGER)
                }
                statement.setInt(6, entity.age)
                val race = entity.race
                if (race != null) {
                    statement.setInt(7, race.id)
                } else {
                    statement.setNull(7, INTEGER)
                }
                statement.setString(8, entity.description)
                statement.setBoolean(9, entity.isDead)
                statement.setString(10, entity.location.world.name)
                statement.setDouble(11, entity.location.x)
                statement.setDouble(12, entity.location.y)
                statement.setDouble(13, entity.location.z)
                statement.setFloat(14, entity.location.yaw)
                statement.setFloat(15, entity.location.pitch)
                statement.setBytes(16, entity.inventoryContents.toByteArray())
                val helmet = entity.helmet
                if (helmet != null) {
                    statement.setBytes(17, helmet.toByteArray())
                } else {
                    statement.setNull(17, BLOB)
                }
                val chestplate = entity.chestplate
                if (chestplate != null) {
                    statement.setBytes(18, chestplate.toByteArray())
                } else {
                    statement.setNull(18, BLOB)
                }
                val leggings = entity.leggings
                if (leggings != null) {
                    statement.setBytes(19, leggings.toByteArray())
                } else {
                    statement.setNull(19, BLOB)
                }
                val boots = entity.boots
                if (boots != null) {
                    statement.setBytes(20, boots.toByteArray())
                } else {
                    statement.setNull(20, BLOB)
                }
                statement.setDouble(21, entity.health)
                statement.setDouble(22, entity.maxHealth)
                statement.setInt(23, entity.mana)
                statement.setInt(24, entity.maxMana)
                statement.setInt(25, entity.foodLevel)
                statement.setInt(26, entity.thirstLevel)
                statement.setBoolean(27, entity.isPlayerHidden)
                statement.setBoolean(28, entity.isProfileHidden)
                statement.setBoolean(29, entity.isNameHidden)
                statement.setBoolean(30, entity.isGenderHidden)
                statement.setBoolean(31, entity.isAgeHidden)
                statement.setBoolean(32, entity.isRaceHidden)
                statement.setBoolean(33, entity.isDescriptionHidden)
                statement.setInt(34, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKCharacter? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var character: RPKCharacter? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, player_id, profile_id, minecraft_profile_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level, player_hidden, profile_hidden, name_hidden, gender_hidden, age_hidden, race_hidden, description_hidden FROM rpkit_character WHERE id = ?").use { statement ->
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                    val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                    val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                    val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
                    val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val helmetBytes = resultSet.getBytes("helmet")
                        val chestplateBytes = resultSet.getBytes("chestplate")
                        val leggingsBytes = resultSet.getBytes("leggings")
                        val bootsBytes = resultSet.getBytes("boots")
                        character = RPKCharacterImpl(
                                plugin = plugin,
                                id = resultSet.getInt("id"),
                                player = playerProvider.getPlayer(resultSet.getInt("player_id")),
                                profile = profileProvider.getProfile(resultSet.getInt("profile_id")),
                                minecraftProfile = minecraftProfileProvider.getMinecraftProfile(resultSet.getInt("minecraft_profile_id")),
                                name = resultSet.getString("name"),
                                gender = genderProvider.getGender(resultSet.getInt("gender_id")),
                                age = resultSet.getInt("age"),
                                race = raceProvider.getRace(resultSet.getInt("race_id")),
                                description = resultSet.getString("description"),
                                dead = resultSet.getBoolean("dead"),
                                location = Location(
                                        Bukkit.getWorld(resultSet.getString("world")),
                                        resultSet.getDouble("x"),
                                        resultSet.getDouble("y"),
                                        resultSet.getDouble("z"),
                                        resultSet.getFloat("yaw"),
                                        resultSet.getFloat("pitch")
                                ),
                                inventoryContents = itemStackArrayFromByteArray(resultSet.getBytes("inventory_contents")),
                                helmet = if (helmetBytes != null) itemStackFromByteArray(helmetBytes) else null,
                                chestplate = if (chestplateBytes != null) itemStackFromByteArray(chestplateBytes) else null,
                                leggings = if (leggingsBytes != null) itemStackFromByteArray(resultSet.getBytes("leggings")) else null,
                                boots = if (bootsBytes != null) itemStackFromByteArray(resultSet.getBytes("boots")) else null,
                                health = resultSet.getDouble("health"),
                                maxHealth = resultSet.getDouble("max_health"),
                                mana = resultSet.getInt("mana"),
                                maxMana = resultSet.getInt("max_mana"),
                                foodLevel = resultSet.getInt("food_level"),
                                thirstLevel = resultSet.getInt("thirst_level"),
                                isPlayerHidden = resultSet.getBoolean("player_hidden"),
                                isProfileHidden = resultSet.getBoolean("profile_hidden"),
                                isNameHidden = resultSet.getBoolean("name_hidden"),
                                isGenderHidden = resultSet.getBoolean("gender_hidden"),
                                isAgeHidden = resultSet.getBoolean("age_hidden"),
                                isRaceHidden = resultSet.getBoolean("race_hidden"),
                                isDescriptionHidden = resultSet.getBoolean("description_hidden")
                        )
                        cache.put(id, character)
                    }
                }
            }
            return character
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKCharacter? {
        var character: RPKCharacter? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_character WHERE minecraft_profile_id = ?"
            ).use { statement ->
                statement.setInt(1, minecraftProfile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    character = get(resultSet.getInt("id"))
                }
            }
        }
        return character
    }

    fun get(profile: RPKProfile): List<RPKCharacter> {
        val characters = mutableListOf<RPKCharacter>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_character WHERE profile_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val character = get(resultSet.getInt("id"))
                    if (character != null) {
                        characters.add(character)
                    }
                }
            }
        }
        return characters
    }

    override fun delete(entity: RPKCharacter) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_character WHERE id = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                if (cache.containsKey(entity.id)) {
                    cache.remove(entity.id)
                }
            }
        }
    }

}
