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

package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterImpl
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceProvider
import com.seventh_root.elysium.core.bukkit.util.itemStackArrayFromByteArray
import com.seventh_root.elysium.core.bukkit.util.itemStackFromByteArray
import com.seventh_root.elysium.core.bukkit.util.toByteArray
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.Bukkit
import org.bukkit.Location
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.BLOB
import java.sql.Types.INTEGER

/**
 * Represents the character table.
 */
class ElysiumCharacterTable: Table<ElysiumCharacter> {

    private val plugin: ElysiumCharactersBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumCharacter>

    constructor(database: Database, plugin: ElysiumCharactersBukkit): super(database, ElysiumCharacter::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumCharacter::class.java,
                        ResourcePoolsBuilder.heap((plugin.server.maxPlayers * 2).toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_character (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "player_id INTEGER," +
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
            database.setTableVersion(this, "0.4.0")
        }
        if (database.getTableVersion(this).equals("0.1.0")) {
            database.setTableVersion(this, "0.1.1")
        }
        if (database.getTableVersion(this).equals("0.1.1")) {
            database.setTableVersion(this, "0.1.2")
        }
        if (database.getTableVersion(this).equals("0.1.2")) {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "ALTER TABLE elysium_character " +
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
    }

    override fun insert(entity: ElysiumCharacter): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_character(player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level, player_hidden, name_hidden, gender_hidden, age_hidden, race_hidden, description_hidden) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                val player = entity.player
                if (player != null) {
                    statement.setInt(1, player.id)
                } else {
                    statement.setNull(2, INTEGER)
                }
                statement.setString(2, entity.name)
                val gender = entity.gender
                if (gender != null) {
                    statement.setInt(3, gender.id)
                } else {
                    statement.setNull(3, INTEGER)
                }
                statement.setInt(4, entity.age)
                val race = entity.race
                if (race != null) {
                    statement.setInt(5, race.id)
                } else {
                    statement.setNull(5, INTEGER)
                }
                statement.setString(6, entity.description)
                statement.setBoolean(7, entity.isDead)
                statement.setString(8, entity.location.world.name)
                statement.setDouble(9, entity.location.x)
                statement.setDouble(10, entity.location.y)
                statement.setDouble(11, entity.location.z)
                statement.setFloat(12, entity.location.yaw)
                statement.setFloat(13, entity.location.pitch)
                statement.setBytes(14, entity.inventoryContents.toByteArray())
                val helmet = entity.helmet
                if (helmet != null) {
                    statement.setBytes(15, helmet.toByteArray())
                } else {
                    statement.setNull(15, BLOB)
                }
                val chestplate = entity.chestplate
                if (chestplate != null) {
                    statement.setBytes(16, chestplate.toByteArray())
                } else {
                    statement.setNull(16, BLOB)
                }
                val leggings = entity.leggings
                if (leggings != null) {
                    statement.setBytes(17, leggings.toByteArray())
                } else {
                    statement.setNull(17, BLOB)
                }
                val boots = entity.boots
                if (boots != null) {
                    statement.setBytes(18, boots.toByteArray())
                } else {
                    statement.setNull(18, BLOB)
                }
                statement.setDouble(19, entity.health)
                statement.setDouble(20, entity.maxHealth)
                statement.setInt(21, entity.mana)
                statement.setInt(22, entity.maxMana)
                statement.setInt(23, entity.foodLevel)
                statement.setInt(24, entity.thirstLevel)
                statement.setBoolean(25, entity.isPlayerHidden)
                statement.setBoolean(26, entity.isNameHidden)
                statement.setBoolean(27, entity.isGenderHidden)
                statement.setBoolean(28, entity.isAgeHidden)
                statement.setBoolean(29, entity.isRaceHidden)
                statement.setBoolean(30, entity.isDescriptionHidden)
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

    override fun update(entity: ElysiumCharacter) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_character SET player_id = ?, name = ?, gender_id = ?, age = ?, race_id = ?, description = ?, dead = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, inventory_contents = ?, helmet = ?, chestplate = ?, leggings = ?, boots = ?, health = ?, max_health = ?, mana = ?, max_mana = ?, food_level = ?, thirst_level = ?, player_hidden = ?, name_hidden = ?, gender_hidden = ?, age_hidden = ?, race_hidden = ?, description_hidden = ? WHERE id = ?").use { statement ->
                val player = entity.player
                if (player != null) {
                    statement.setInt(1, player.id)
                } else {
                    statement.setNull(1, INTEGER)
                }
                statement.setString(2, entity.name)
                val gender = entity.gender
                if (gender != null) {
                    statement.setInt(3, gender.id)
                } else {
                    statement.setNull(3, INTEGER)
                }
                statement.setInt(4, entity.age)
                val race = entity.race
                if (race != null) {
                    statement.setInt(5, race.id)
                } else {
                    statement.setNull(5, INTEGER)
                }
                statement.setString(6, entity.description)
                statement.setBoolean(7, entity.isDead)
                statement.setString(8, entity.location.world.name)
                statement.setDouble(9, entity.location.x)
                statement.setDouble(10, entity.location.y)
                statement.setDouble(11, entity.location.z)
                statement.setFloat(12, entity.location.yaw)
                statement.setFloat(13, entity.location.pitch)
                statement.setBytes(14, entity.inventoryContents.toByteArray())
                val helmet = entity.helmet
                if (helmet != null) {
                    statement.setBytes(15, helmet.toByteArray())
                } else {
                    statement.setNull(15, BLOB)
                }
                val chestplate = entity.chestplate
                if (chestplate != null) {
                    statement.setBytes(16, chestplate.toByteArray())
                } else {
                    statement.setNull(16, BLOB)
                }
                val leggings = entity.leggings
                if (leggings != null) {
                    statement.setBytes(17, leggings.toByteArray())
                } else {
                    statement.setNull(17, BLOB)
                }
                val boots = entity.boots
                if (boots != null) {
                    statement.setBytes(18, boots.toByteArray())
                } else {
                    statement.setNull(18, BLOB)
                }
                statement.setDouble(19, entity.health)
                statement.setDouble(20, entity.maxHealth)
                statement.setInt(21, entity.mana)
                statement.setInt(22, entity.maxMana)
                statement.setInt(23, entity.foodLevel)
                statement.setInt(24, entity.thirstLevel)
                statement.setBoolean(25, entity.isPlayerHidden)
                statement.setBoolean(26, entity.isNameHidden)
                statement.setBoolean(27, entity.isGenderHidden)
                statement.setBoolean(28, entity.isAgeHidden)
                statement.setBoolean(29, entity.isRaceHidden)
                statement.setBoolean(30, entity.isDescriptionHidden)
                statement.setInt(31, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): ElysiumCharacter? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var character: ElysiumCharacter? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level, player_hidden, name_hidden, gender_hidden, age_hidden, race_hidden, description_hidden FROM elysium_character WHERE id = ?").use { statement ->
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                    val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class)
                    val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class)
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val helmetBytes = resultSet.getBytes("helmet")
                        val chestplateBytes = resultSet.getBytes("chestplate")
                        val leggingsBytes = resultSet.getBytes("leggings")
                        val bootsBytes = resultSet.getBytes("boots")
                        character = ElysiumCharacterImpl(
                                plugin = plugin,
                                id = resultSet.getInt("id"),
                                player = playerProvider.getPlayer(resultSet.getInt("player_id")),
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

    override fun delete(entity: ElysiumCharacter) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_character WHERE id = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                if (cache.containsKey(entity.id)) {
                    cache.remove(entity.id)
                }
            }
        }
    }

}
