package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKPlayerUnclaiming
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import java.sql.PreparedStatement
import java.sql.Statement


class RPKPlayerUnclaimingTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKPlayerUnclaiming>(database, RPKPlayerUnclaiming::class) {
    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_player_unclaiming(" +
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
                        "TRUNCATE rpkit_player_unclaiming"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE rpkit_player_unclaiming " +
                                "DROP COLUMN player_id," +
                                "ADD COLUMN minecraft_profile_id INTEGER AFTER id"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKPlayerUnclaiming): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_player_unclaiming(minecraft_profile_id) VALUES(?)",
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

    override fun update(entity: RPKPlayerUnclaiming) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_player_unclaiming SET minecraft_profile_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setInt(2, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKPlayerUnclaiming? {
        var minecraftProfileUnclaiming: RPKPlayerUnclaiming? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, minecraft_profile_id FROM rpkit_player_unclaiming WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val minecraftProfile = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))
                    if (minecraftProfile != null) {
                        minecraftProfileUnclaiming = RPKPlayerUnclaiming(
                                resultSet.getInt("id"),
                                minecraftProfile
                        )
                    } else {
                        connection.prepareStatement(
                                "DELETE FROM rpkit_player_unclaiming"
                        )
                    }
                }
            }
        }
        return minecraftProfileUnclaiming
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKPlayerUnclaiming? {
        var minecraftProfileUnclaiming: RPKPlayerUnclaiming? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, minecraft_profile_id FROM rpkit_player_unclaiming WHERE minecraft_profile_id = ?"
            ).use { statement ->
                statement.setInt(1, minecraftProfile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    minecraftProfileUnclaiming = RPKPlayerUnclaiming(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))!!
                    )
                }
            }
        }
        return minecraftProfileUnclaiming
    }

    override fun delete(entity: RPKPlayerUnclaiming) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_player_unclaiming WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}