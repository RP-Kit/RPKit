package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
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

class BukkitCharacterTable: Table<BukkitCharacter> {

    private val plugin: ElysiumCharactersBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, BukkitCharacter>

    constructor(database: Database, plugin: ElysiumCharactersBukkit): super(database, BukkitCharacter::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, BukkitCharacter::class.java,
                        ResourcePoolsBuilder.heap((plugin.server.maxPlayers * 2).toLong())).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_character (" +
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
                            "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)," +
                            "FOREIGN KEY(gender_id) REFERENCES bukkit_gender(id)," +
                            "FOREIGN KEY(race_id) REFERENCES bukkit_race(id)" +
                        ")").use({ statement -> statement.executeUpdate() })
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
                                "UNIQUE(player_id)," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)," +
                                "FOREIGN KEY(character_id) REFERENCES bukkit_character(id)" +
                        ")").use({ statement -> statement.executeUpdate() })
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

    override fun insert(`object`: BukkitCharacter): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_character(player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
                    val player = `object`.player
                    if (player != null) {
                        statement.setInt(1, player.id)
                    } else {
                        statement.setNull(2, INTEGER)
                    }
                    statement.setString(2, `object`.name)
                    val gender = `object`.gender
                    if (gender != null) {
                        statement.setInt(3, gender.id)
                    } else {
                        statement.setNull(3, INTEGER)
                    }
                    statement.setInt(4, `object`.age)
                    val race = `object`.race
                    if (race != null) {
                        statement.setInt(5, race.id)
                    } else {
                        statement.setNull(5, INTEGER)
                    }
                    statement.setString(6, `object`.description)
                    statement.setBoolean(7, `object`.isDead)
                    statement.setString(8, `object`.location.world.name)
                    statement.setDouble(9, `object`.location.x)
                    statement.setDouble(10, `object`.location.y)
                    statement.setDouble(11, `object`.location.z)
                    statement.setFloat(12, `object`.location.yaw)
                    statement.setFloat(13, `object`.location.pitch)
                    statement.setBytes(14, serializeInventory(`object`.inventoryContents))
                    statement.setBytes(15, serializeItemStack(`object`.helmet))
                    statement.setBytes(16, serializeItemStack(`object`.chestplate))
                    statement.setBytes(17, serializeItemStack(`object`.leggings))
                    statement.setBytes(18, serializeItemStack(`object`.boots))
                    statement.setDouble(19, `object`.health)
                    statement.setDouble(20, `object`.maxHealth)
                    statement.setInt(21, `object`.mana)
                    statement.setInt(22, `object`.maxMana)
                    statement.setInt(23, `object`.foodLevel)
                    statement.setInt(24, `object`.thirstLevel)
                    statement.executeUpdate()
                    val generatedKeys = statement.generatedKeys
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1)
                        `object`.id = id
                        cache.put(id, `object`)
                    }
                })
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

    override fun update(`object`: BukkitCharacter) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_character SET player_id = ?, name = ?, gender_id = ?, age = ?, race_id = ?, description = ?, dead = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, inventory_contents = ?, helmet = ?, chestplate = ?, leggings = ?, boots = ?, health = ?, max_health = ?, mana = ?, max_mana = ?, food_level = ?, thirst_level = ? WHERE id = ?").use({ statement ->
                    val player = `object`.player
                    if (player != null) {
                        statement.setInt(1, player.id)
                    } else {
                        statement.setNull(1, INTEGER)
                    }
                    statement.setString(2, `object`.name)
                    val gender = `object`.gender
                    if (gender != null) {
                        statement.setInt(3, gender.id)
                    } else {
                        statement.setNull(3, INTEGER)
                    }
                    statement.setInt(4, `object`.age)
                    val race = `object`.race
                    if (race != null) {
                        statement.setInt(5, race.id)
                    } else {
                        statement.setNull(5, INTEGER)
                    }
                    statement.setString(6, `object`.description)
                    statement.setBoolean(7, `object`.isDead)
                    statement.setString(8, `object`.location.world.name)
                    statement.setDouble(9, `object`.location.x)
                    statement.setDouble(10, `object`.location.y)
                    statement.setDouble(11, `object`.location.z)
                    statement.setFloat(12, `object`.location.yaw)
                    statement.setFloat(13, `object`.location.pitch)
                    statement.setBytes(14, serializeInventory(`object`.inventoryContents))
                    statement.setBytes(15, serializeItemStack(`object`.helmet))
                    statement.setBytes(16, serializeItemStack(`object`.chestplate))
                    statement.setBytes(17, serializeItemStack(`object`.leggings))
                    statement.setBytes(18, serializeItemStack(`object`.boots))
                    statement.setDouble(19, `object`.health)
                    statement.setDouble(20, `object`.maxHealth)
                    statement.setInt(21, `object`.mana)
                    statement.setInt(22, `object`.maxMana)
                    statement.setInt(23, `object`.foodLevel)
                    statement.setInt(24, `object`.thirstLevel)
                    statement.setInt(25, `object`.id)
                    statement.executeUpdate()
                    cache.put(`object`.id, `object`)
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitCharacter? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            try {
                var character: BukkitCharacter? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, inventory_contents, helmet, chestplate, leggings, boots, health, max_health, mana, max_mana, food_level, thirst_level FROM bukkit_character WHERE id = ?").use({ statement ->
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                        val genderProvider = plugin.core.serviceManager.getServiceProvider(BukkitGenderProvider::class.java)
                        val raceProvider = plugin.core.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            character = BukkitCharacter(
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
                    })
                }
                return character
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }

        return null
    }

    override fun delete(`object`: BukkitCharacter) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_character WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                    if (cache.containsKey(`object`.id)) {
                        cache.remove(`object`.id)
                    }
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
