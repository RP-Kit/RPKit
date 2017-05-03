package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessagesEnabled
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKLogMessagesEnabledTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKLogMessagesEnabled>(database, RPKLogMessagesEnabled::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_log_messages_enabled(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "minecraft_profile_id INTEGER," +
                            "enabled BOOLEAN" +
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
                        "TRUNCATE rpkit_log_messages_enabled"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE rpkit_log_messages_enabled " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN minecraft_profile_id INTEGER AFTER id"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKLogMessagesEnabled): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_log_messages_enabled(minecraft_profile_id, enabled) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setBoolean(2, entity.enabled)
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

    override fun update(entity: RPKLogMessagesEnabled) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_log_messages_enabled SET minecraft_profile_id = ?, enabled = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setBoolean(2, entity.enabled)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKLogMessagesEnabled? {
        var logMessagesEnabled: RPKLogMessagesEnabled? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, player_id, enabled FROM rpkit_log_messages_enabled WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))
                    if (minecraftProfile != null) {
                        logMessagesEnabled = RPKLogMessagesEnabled(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class).getMinecraftProfile(resultSet.getInt("minecraft_profile_id"))!!,
                                resultSet.getBoolean("enabled")
                        )
                    } else {
                        connection.prepareStatement(
                                "DELETE FROM rpkit_log_messages_enabled WHERE id = ?"
                        ).use { statement ->
                            statement.setInt(1, resultSet.getInt("id"))
                            statement.executeUpdate()
                        }
                    }
                }
            }
        }
        return logMessagesEnabled
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKLogMessagesEnabled? {
        var logMessagesEnabled: RPKLogMessagesEnabled? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, minecraft_profile_id, enabled FROM rpkit_log_messages_enabled WHERE minecraft_profile_id = ?"
            ).use { statement ->
                statement.setInt(1, minecraftProfile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    logMessagesEnabled = RPKLogMessagesEnabled(
                            resultSet.getInt("id"),
                            minecraftProfile,
                            resultSet.getBoolean("enabled")
                    )
                }
            }
        }
        return logMessagesEnabled
    }

    override fun delete(entity: RPKLogMessagesEnabled) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_log_messages_enabled WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}