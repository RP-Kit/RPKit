package com.rpkit.essentials.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.tracking.RPKTrackingEnabled
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKTrackingEnabledTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKTrackingEnabled>(database, RPKTrackingEnabled::class) {
    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_tracking_enabled(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "enabled BOOLEAN" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKTrackingEnabled): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_tracking_enabled(character_id, enabled) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
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

    override fun update(entity: RPKTrackingEnabled) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_tracking_enabled SET character_id = ?, enabled = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setBoolean(2, entity.enabled)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKTrackingEnabled? {
        var trackingEnabled: RPKTrackingEnabled? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, character_id, enabled FROM rpkit_tracking_enabled WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    trackingEnabled = RPKTrackingEnabled(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                            resultSet.getBoolean("enabled")
                    )
                }
            }
        }
        return trackingEnabled
    }

    fun get(character: RPKCharacter): RPKTrackingEnabled? {
        var trackingEnabled: RPKTrackingEnabled? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, character_id, enabled FROM rpkit_tracking_enabled WHERE character_id = ?"
            ).use { statement ->
                statement.setInt(1, character.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    trackingEnabled = RPKTrackingEnabled(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                            resultSet.getBoolean("enabled")
                    )
                }
            }
        }
        return trackingEnabled
    }

    override fun delete(entity: RPKTrackingEnabled) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_tracking_enabled WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }
}