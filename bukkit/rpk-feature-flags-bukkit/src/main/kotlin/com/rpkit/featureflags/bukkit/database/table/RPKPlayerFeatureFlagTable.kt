package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagProvider
import com.rpkit.featureflags.bukkit.featureflag.RPKPlayerFeatureFlag
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import java.sql.PreparedStatement


class RPKPlayerFeatureFlagTable(database: Database, private val plugin: RPKFeatureFlagsBukkit) : Table<RPKPlayerFeatureFlag>(database, RPKPlayerFeatureFlag::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_player_feature_flag(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "feature_flag_id INTEGER," +
                            "enabled BOOLEAN" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKPlayerFeatureFlag): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_player_feature_flag(player_id, feature_flag_id, enabled) VALUES(?, ?, ?)"
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.featureFlag.id)
                statement.setBoolean(3, entity.enabled)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKPlayerFeatureFlag) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_player_feature_flag SET player_id = ?, feature_flag_id = ?, enabled = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.player.id)
                statement.setInt(2, entity.featureFlag.id)
                statement.setBoolean(3, entity.enabled)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKPlayerFeatureFlag? {
        var playerFeatureFlag: RPKPlayerFeatureFlag? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, player_id, feature_flag_id, enabled FROM rpkit_player_feature_flag WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    playerFeatureFlag = RPKPlayerFeatureFlag(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!,
                            plugin.core.serviceManager.getServiceProvider(RPKFeatureFlagProvider::class).getFeatureFlag(resultSet.getInt("feature_flag_id"))!!,
                            resultSet.getBoolean("enabled")
                    )
                }
            }
        }
        return playerFeatureFlag
    }

    fun get(player: RPKPlayer, featureFlag: RPKFeatureFlag): RPKPlayerFeatureFlag? {
        var playerFeatureFlag: RPKPlayerFeatureFlag? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, player_id, feature_flag_id, enabled FROM rpkit_player_feature_flag WHERE player_id = ? AND feature_flag_id = ?"
            ).use { statement ->
                statement.setInt(1, player.id)
                statement.setInt(2, featureFlag.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    playerFeatureFlag = RPKPlayerFeatureFlag(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(resultSet.getInt("player_id"))!!,
                            plugin.core.serviceManager.getServiceProvider(RPKFeatureFlagProvider::class).getFeatureFlag(resultSet.getInt("feature_flag_id"))!!,
                            resultSet.getBoolean("enabled")
                    )
                }
            }
        }
        return playerFeatureFlag
    }

    override fun delete(entity: RPKPlayerFeatureFlag) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_player_feature_flag WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}