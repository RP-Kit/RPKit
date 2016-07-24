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
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.INTEGER

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
        try {
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
                            "thirst_level INTEGER" +
                        ")").use { statement ->
                    statement.executeUpdate()
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS player_character(" +
                                "player_id INTEGER," +
                                "character_id INTEGER," +
                                "UNIQUE(player_id)" +
                        ")").use { statement ->
                    statement.executeUpdate()
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this).equals("0.1.0")) {
            database.setTableVersion(this, "0.1.1")
        }
        if (database.getTableVersion(this).equals("0.1.1")) {
            database.setTableVersion(this, "0.1.2")
        }
    }

    override fun insert(entity: ElysiumCharacter): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO elysium_character(player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                    statement.setBytes(14, serializeInventory(entity.inventoryContents))
                    statement.setBytes(15, serializeItemStack(entity.helmet))
                    statement.setBytes(16, serializeItemStack(entity.chestplate))
                    statement.setBytes(17, serializeItemStack(entity.leggings))
                    statement.setBytes(18, serializeItemStack(entity.boots))
                    statement.setDouble(19, entity.health)
                    statement.setDouble(20, entity.maxHealth)
                    statement.setInt(21, entity.mana)
                    statement.setInt(22, entity.maxMana)
                    statement.setInt(23, entity.foodLevel)
                    statement.setInt(24, entity.thirstLevel)
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
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return 0
    }

    private fun serializeItemStack(itemStack: ItemStack?): ByteArray? {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
                    bukkitObjectOutputStream.writeObject(itemStack)
                    return byteArrayOutputStream.toByteArray()
                }
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
        return null
    }

    private fun deserializeItemStack(bytes: ByteArray): ItemStack? {
        try {
            ByteArrayInputStream(bytes).use { byteArrayInputStream -> BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream -> return bukkitObjectInputStream.readObject() as ItemStack? } }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return null
    }

    private fun serializeInventory(inventoryContents: Array<ItemStack>): ByteArray? {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
                    bukkitObjectOutputStream.writeObject(inventoryContents)
                    return byteArrayOutputStream.toByteArray()
                }
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
        }

        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeInventory(bytes: ByteArray): Array<ItemStack> {
        try {
            ByteArrayInputStream(bytes)
                    .use { byteArrayInputStream -> BukkitObjectInputStream(byteArrayInputStream)
                            .use { bukkitObjectInputStream -> return bukkitObjectInputStream.readObject() as Array<ItemStack> } }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return emptyArray()
    }

    override fun update(entity: ElysiumCharacter) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE elysium_character SET player_id = ?, name = ?, gender_id = ?, age = ?, race_id = ?, description = ?, dead = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, inventory_contents = ?, helmet = ?, chestplate = ?, leggings = ?, boots = ?, health = ?, max_health = ?, mana = ?, max_mana = ?, food_level = ?, thirst_level = ? WHERE id = ?").use { statement ->
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
                    statement.setBytes(14, serializeInventory(entity.inventoryContents))
                    statement.setBytes(15, serializeItemStack(entity.helmet))
                    statement.setBytes(16, serializeItemStack(entity.chestplate))
                    statement.setBytes(17, serializeItemStack(entity.leggings))
                    statement.setBytes(18, serializeItemStack(entity.boots))
                    statement.setDouble(19, entity.health)
                    statement.setDouble(20, entity.maxHealth)
                    statement.setInt(21, entity.mana)
                    statement.setInt(22, entity.maxMana)
                    statement.setInt(23, entity.foodLevel)
                    statement.setInt(24, entity.thirstLevel)
                    statement.setInt(25, entity.id)
                    statement.executeUpdate()
                    cache.put(entity.id, entity)
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): ElysiumCharacter? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            try {
                var character: ElysiumCharacter? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level FROM elysium_character WHERE id = ?").use { statement ->
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                        val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class)
                        val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class)
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
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
                                    inventoryContents = deserializeInventory(resultSet.getBytes("inventory_contents")),
                                    helmet = deserializeItemStack(resultSet.getBytes("helmet")),
                                    chestplate = deserializeItemStack(resultSet.getBytes("chestplate")),
                                    leggings = deserializeItemStack(resultSet.getBytes("leggings")),
                                    boots = deserializeItemStack(resultSet.getBytes("boots")),
                                    health = resultSet.getDouble("health"),
                                    maxHealth = resultSet.getDouble("max_health"),
                                    mana = resultSet.getInt("mana"),
                                    maxMana = resultSet.getInt("max_mana"),
                                    foodLevel = resultSet.getInt("food_level"),
                                    thirstLevel = resultSet.getInt("thirst_level")
                            )
                            cache.put(id, character)
                        }
                    }
                }
                return character
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }

        return null
    }

    override fun delete(entity: ElysiumCharacter) {
        try {
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
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
