package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class BukkitCharacterTable @Throws(SQLException::class)
constructor(database: Database, private val plugin: ElysiumCharactersBukkit) : Table<BukkitCharacter>(database, BukkitCharacter::class.java) {

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
                                "helmet BLOB," +
                                "chestplate BLOB," +
                                "leggings BLOB," +
                                "boots BLOB," +
                                "inventory_contents BLOB," +
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

    }

    override fun insert(`object`: BukkitCharacter): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_character(player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, helmet, chestplate, leggings, boots, inventory_contents, health, max_health, mana, max_mana, food_level, thirst_level) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
                    statement.setInt(1, `object`.player.id)
                    statement.setString(2, `object`.name)
                    statement.setInt(3, `object`.gender.id)
                    statement.setInt(4, `object`.age)
                    statement.setInt(5, `object`.race.id)
                    statement.setString(6, `object`.description)
                    statement.setBoolean(7, `object`.isDead)
                    statement.setString(8, `object`.location.world.name)
                    statement.setDouble(9, `object`.location.x)
                    statement.setDouble(10, `object`.location.y)
                    statement.setDouble(11, `object`.location.z)
                    statement.setFloat(12, `object`.location.yaw)
                    statement.setFloat(13, `object`.location.pitch)
                    statement.setBytes(14, serializeItemStack(`object`.helmet))
                    statement.setBytes(15, serializeItemStack(`object`.chestplate))
                    statement.setBytes(16, serializeItemStack(`object`.leggings))
                    statement.setBytes(17, serializeItemStack(`object`.boots))
                    statement.setBytes(18, serializeInventory(`object`.inventoryContents))
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
    private fun deserializeInventory(bytes: ByteArray): Array<ItemStack>? {
        try {
            ByteArrayInputStream(bytes).use { byteArrayInputStream -> BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream -> return bukkitObjectInputStream.readObject() as Array<ItemStack>? } }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return null
    }

    override fun update(`object`: BukkitCharacter) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_character SET player_id = ?, name = ?, gender_id = ?, age = ?, race_id = ?, description = ?, dead = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, helmet = ?, chestplate = ?, leggings = ?, boots = ?, inventory_contents = ?, health = ?, max_health = ?, mana = ?, max_mana = ?, food_level = ?, thirst_level = ? WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.player.id)
                    statement.setString(2, `object`.name)
                    statement.setInt(3, `object`.gender.id)
                    statement.setInt(4, `object`.age)
                    statement.setInt(5, `object`.race.id)
                    statement.setString(6, `object`.description)
                    statement.setBoolean(7, `object`.isDead)
                    statement.setString(8, `object`.location.world.name)
                    statement.setDouble(9, `object`.location.x)
                    statement.setDouble(10, `object`.location.y)
                    statement.setDouble(11, `object`.location.z)
                    statement.setFloat(12, `object`.location.yaw)
                    statement.setFloat(13, `object`.location.pitch)
                    statement.setBytes(14, serializeItemStack(`object`.helmet))
                    statement.setBytes(15, serializeItemStack(`object`.chestplate))
                    statement.setBytes(16, serializeItemStack(`object`.leggings))
                    statement.setBytes(17, serializeItemStack(`object`.boots))
                    statement.setBytes(18, serializeInventory(`object`.inventoryContents))
                    statement.setDouble(19, `object`.health)
                    statement.setDouble(20, `object`.maxHealth)
                    statement.setInt(21, `object`.mana)
                    statement.setInt(22, `object`.maxMana)
                    statement.setInt(23, `object`.foodLevel)
                    statement.setInt(24, `object`.thirstLevel)
                    statement.setInt(25, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitCharacter? {
        try {
            var character: BukkitCharacter? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, helmet, chestplate, leggings, boots, inventory_contents, health, max_health, mana, max_mana, food_level, thirst_level FROM bukkit_character WHERE id = ?").use({ statement ->
                    val playerProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                    val genderProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitGenderProvider::class.java)
                    val raceProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        character = BukkitCharacter.Builder(plugin)
                                .id(resultSet.getInt("id"))
                                .player(playerProvider.getPlayer(resultSet.getInt("player_id"))!!)
                                .name(resultSet.getString("name"))
                                .gender(genderProvider.getGender(resultSet.getInt("gender_id"))!!)
                                .age(resultSet.getInt("age"))
                                .race(raceProvider.getRace(resultSet.getInt("race_id"))!!)
                                .description(resultSet.getString("description"))
                                .dead(resultSet.getBoolean("dead"))
                                .location(
                                    Location(
                                            Bukkit.getWorld(resultSet.getString("world")),
                                            resultSet.getDouble("x"),
                                            resultSet.getDouble("y"),
                                            resultSet.getDouble("z"),
                                            resultSet.getFloat("yaw"),
                                            resultSet.getFloat("pitch")
                                    )
                                )
                                .helmet(deserializeItemStack(resultSet.getBytes("helmet")))
                                .chestplate(deserializeItemStack(resultSet.getBytes("chestplate")))
                                .leggings(deserializeItemStack(resultSet.getBytes("leggings")))
                                .boots(deserializeItemStack(resultSet.getBytes("boots")))
                                .inventoryContents(deserializeInventory(resultSet.getBytes("inventory_contents")))
                                .health(resultSet.getDouble("health"))
                                .maxHealth(resultSet.getDouble("max_health"))
                                .mana(resultSet.getInt("mana"))
                                .maxMana(resultSet.getInt("max_mana"))
                                .foodLevel(resultSet.getInt("food_level"))
                                .thirstLevel(resultSet.getInt("thirst_level"))
                                .build()
                    }
                })
            }
            return character
        } catch (exception: SQLException) {
            exception.printStackTrace()
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
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
