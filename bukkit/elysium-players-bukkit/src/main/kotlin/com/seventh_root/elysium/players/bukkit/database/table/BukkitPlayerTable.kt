package com.seventh_root.elysium.players.bukkit.database.table

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*

class BukkitPlayerTable @Throws(SQLException::class)
constructor(database: Database) : Table<BukkitPlayer>(database, BukkitPlayer::class.java) {

    override fun create() {
        try {
            database.createConnection().use { connection: Connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_player(" +
                                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                "minecraft_uuid VARCHAR(36)" +
                                ")").use({ statement: PreparedStatement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun insert(`object`: BukkitPlayer): Int {
        try {
            var id: Int = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_player(minecraft_uuid) VALUES(?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
                    statement.setString(1, `object`.bukkitPlayer.uniqueId.toString())
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

    override fun update(`object`: BukkitPlayer) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_player SET minecraft_uuid = ? WHERE id = ?").use({ statement ->
                    statement.setString(1, `object`.bukkitPlayer.uniqueId.toString())
                    statement.setInt(2, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitPlayer? {
        try {
            var player: BukkitPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, minecraft_uuid FROM bukkit_player WHERE id = ?").use({ statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        player = BukkitPlayer(resultSet.getInt("id"), Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("minecraft_uuid"))))
                    }
                })
            }
            return player
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    operator fun get(bukkitPlayer: OfflinePlayer): BukkitPlayer? {
        try {
            var player: BukkitPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, minecraft_uuid FROM bukkit_player WHERE minecraft_uuid = ?").use({ statement ->
                    statement.setString(1, bukkitPlayer.uniqueId.toString())
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        player = BukkitPlayer(resultSet.getInt("id"), Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("minecraft_uuid"))))
                    }
                })
            }
            return player
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    override fun delete(`object`: BukkitPlayer) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_player WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
