package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKPlayerGettingKey
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import java.sql.PreparedStatement
import java.sql.Statement


class RPKPlayerGettingKeyTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKPlayerGettingKey>(database, RPKPlayerGettingKey::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_player_getting_key(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER" +
                            ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKPlayerGettingKey): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_player_getting_key(player_id) VALUES(?)",
                    Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.player.id)
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

    override fun update(entity: RPKPlayerGettingKey) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_player_getting_key SET player_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKPlayerGettingKey? {
        var playerGettingKey: RPKPlayerGettingKey? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, player_id FROM rpkit_player_getting_key WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    playerGettingKey = RPKPlayerGettingKey(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                    )
                }
            }
        }
        return playerGettingKey
    }

    fun get(player: RPKPlayer): RPKPlayerGettingKey? {
        var playerGettingKey: RPKPlayerGettingKey? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, player_id FROM rpkit_player_getting_key WHERE player_id = ?"
            ).use { statement ->
                statement.setInt(1, player.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    playerGettingKey = RPKPlayerGettingKey(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!
                    )
                }
            }
        }
        return playerGettingKey
    }

    override fun delete(entity: RPKPlayerGettingKey) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_player_getting_key WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }
    
}