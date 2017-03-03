package com.rpkit.travel.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.warp.RPKWarpImpl
import com.rpkit.warp.bukkit.warp.RPKWarp
import org.bukkit.Location
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKWarpTable(database: Database, private val plugin: RPKTravelBukkit): Table<RPKWarp>(database, RPKWarp::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_warp(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                            "name VARCHAR(256), " +
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

    override fun insert(entity: RPKWarp): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_warp(name, world, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.name)
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

    override fun update(entity: RPKWarp) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_warp SET name = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, entity.name)
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

    override fun get(id: Int): RPKWarp? {
        var warp: RPKWarp? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, name, world, x, y, z, yaw, pitch FROM rpkit_warp WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    warp = RPKWarpImpl(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
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
        return warp
    }

    fun get(name: String): RPKWarp? {
        var warp: RPKWarp? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, name, world, x, y, z, yaw, pitch FROM rpkit_warp WHERE name = ?"
            ).use { statement ->
                statement.setString(1, name)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    warp = RPKWarpImpl(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
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
        return warp
    }

    fun getAll(): List<RPKWarp> {
        val warps = mutableListOf<RPKWarp>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_warp"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val warp = get(resultSet.getInt(1))
                    if (warp != null) {
                        warps.add(warp)
                    }
                }
            }
        }
        return warps
    }

    override fun delete(entity: RPKWarp) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_warp WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}