package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockedBlock
import org.bukkit.block.Block
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKLockedBlockTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKLockedBlock>(database, RPKLockedBlock::class) {
    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_locked_block(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "world VARCHAR(256)," +
                            "x INTEGER," +
                            "y INTEGER," +
                            "z INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKLockedBlock): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_locked_block(world, x, y, z) VALUES(?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.block.world.name)
                statement.setInt(2, entity.block.x)
                statement.setInt(3, entity.block.y)
                statement.setInt(4, entity.block.z)
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

    override fun update(entity: RPKLockedBlock) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_locked_block SET world = ?, x = ?, y = ?, z = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, entity.block.world.name)
                statement.setInt(2, entity.block.x)
                statement.setInt(3, entity.block.y)
                statement.setInt(4, entity.block.z)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKLockedBlock? {
        var lockedBlock: RPKLockedBlock? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, world, x, y, z FROM rpkit_locked_block WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    lockedBlock = RPKLockedBlock(
                            resultSet.getInt("id"),
                            plugin.server.getWorld(resultSet.getString("world")).getBlockAt(
                                    resultSet.getInt("x"),
                                    resultSet.getInt("y"),
                                    resultSet.getInt("z")
                            )
                    )
                }
            }
        }
        return lockedBlock
    }

    fun get(block: Block): RPKLockedBlock? {
        var lockedBlock: RPKLockedBlock? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, world, x, y, z FROM rpkit_locked_block WHERE world = ? AND x = ? AND y = ? AND z = ?"
            ).use { statement ->
                statement.setString(1, block.world.name)
                statement.setInt(2, block.x)
                statement.setInt(3, block.y)
                statement.setInt(4, block.z)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    lockedBlock = RPKLockedBlock(
                            resultSet.getInt("id"),
                            plugin.server.getWorld(resultSet.getString("world")).getBlockAt(
                                    resultSet.getInt("x"),
                                    resultSet.getInt("y"),
                                    resultSet.getInt("z")
                            )
                    )
                }
            }
        }
        return lockedBlock
    }

    override fun delete(entity: RPKLockedBlock) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_locked_block WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }
}