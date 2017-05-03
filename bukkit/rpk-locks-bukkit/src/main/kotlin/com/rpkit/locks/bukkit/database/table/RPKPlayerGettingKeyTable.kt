package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKPlayerGettingKey
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import java.sql.PreparedStatement
import java.sql.Statement


class RPKPlayerGettingKeyTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKPlayerGettingKey>(database, RPKPlayerGettingKey::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_player_getting_key(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "minecraft_profile_id INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "TRUNCATE rpkit_player_getting_key"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE rpkit_player_getting_key " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN minecraft_profile_id INTEGER AFTER id"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKPlayerGettingKey): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_player_getting_key(minecraft_profile_id) VALUES(?)",
                    Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
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
                    "UPDATE rpkit_player_getting_key SET minecraft_profile_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setInt(2, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKPlayerGettingKey? {
        var playerGettingKey: RPKPlayerGettingKey? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, minecraft_profile_id FROM rpkit_player_getting_key WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val minecraftProfile = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))
                    if (minecraftProfile != null) {
                        playerGettingKey = RPKPlayerGettingKey(
                                resultSet.getInt("id"),
                                minecraftProfile
                        )
                    } else {
                        connection.prepareStatement(
                                "DELETE FROM rpkit_player_getting_key WHERE id = ?"
                        ).use { statement ->
                            statement.setInt(1, resultSet.getInt("id"))
                            statement.executeUpdate()
                        }
                    }
                }
            }
        }
        return playerGettingKey
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKPlayerGettingKey? {
        var playerGettingKey: RPKPlayerGettingKey? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, minecraft_profile_id FROM rpkit_player_getting_key WHERE minecraft_profile_id = ?"
            ).use { statement ->
                statement.setInt(1, minecraftProfile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    playerGettingKey = RPKPlayerGettingKey(
                            resultSet.getInt("id"),
                            minecraftProfile
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