package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.locationhistory.RPKPreviousLocation
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.Location
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKPreviousLocationTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKPreviousLocation>(database, RPKPreviousLocation::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_previous_location(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "world VARCHAR(256)," +
                            "x DOUBLE," +
                            "y DOUBLE," +
                            "z DOUBLE," +
                            "yaw REAL," +
                            "pitch REAL" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKPreviousLocation): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement("INSERT INTO rpkit_previous_location(player_id, world, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS)
                    .use { statement ->
                        statement.setInt(1, entity.player.id)
                        statement.setString(2, entity.location.world.name)
                        statement.setDouble(3, entity.location.x)
                        statement.setDouble(4, entity.location.y)
                        statement.setDouble(5, entity.location.z)
                        statement.setFloat(6, entity.location.yaw)
                        statement.setFloat(7, entity.location.pitch)
                        statement.executeUpdate()
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            id = generatedKeys.getInt(1)
                            entity.id = id
                        }
            }
        }
        return id
    }

    override fun update(entity: RPKPreviousLocation) {
        database.createConnection().use { connection ->
            connection.prepareStatement("UPDATE rpkit_previous_location SET player_id = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE id = ?").use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setString(2, entity.location.world.name)
                statement.setDouble(3, entity.location.x)
                statement.setDouble(4, entity.location.y)
                statement.setDouble(5, entity.location.z)
                statement.setFloat(6, entity.location.yaw)
                statement.setFloat(7, entity.location.pitch)
                statement.setInt(8, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKPreviousLocation? {
        var previousLocation: RPKPreviousLocation? = null
        database.createConnection().use { connection ->
            connection.prepareStatement("SELECT id, player_id, world, x, y, z, yaw, pitch FROM rpkit_previous_location WHERE id = ?").use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    previousLocation = RPKPreviousLocation(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!,
                            Location(
                                    plugin.server.getWorld(resultSet.getString("world")),
                                    resultSet.getDouble("x"),
                                    resultSet.getDouble("y"),
                                    resultSet.getDouble("z"),
                                    resultSet.getFloat("yaw"),
                                    resultSet.getFloat("pitch")
                            )
                    )
                }
            }
        }
        return previousLocation
    }

    fun get(player: RPKPlayer): RPKPreviousLocation? {
        var previousLocation: RPKPreviousLocation? = null
        database.createConnection().use { connection ->
            connection.prepareStatement("SELECT id, player_id, world, x, y, z, yaw, pitch FROM rpkit_previous_location WHERE player_id = ?").use { statement ->
                statement.setInt(1, player.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    previousLocation = RPKPreviousLocation(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!,
                            Location(
                                    plugin.server.getWorld(resultSet.getString("world")),
                                    resultSet.getDouble("x"),
                                    resultSet.getDouble("y"),
                                    resultSet.getDouble("z"),
                                    resultSet.getFloat("yaw"),
                                    resultSet.getFloat("pitch")
                            )
                    )
                }
            }
        }
        return previousLocation
    }

    override fun delete(entity: RPKPreviousLocation) {
        database.createConnection().use { connection ->
            connection.prepareStatement("DELETE FROM rpkit_previous_location WHERE id = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}