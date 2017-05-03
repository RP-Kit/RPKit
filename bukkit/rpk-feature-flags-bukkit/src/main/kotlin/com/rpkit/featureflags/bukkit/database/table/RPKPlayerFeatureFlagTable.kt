package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagProvider
import com.rpkit.featureflags.bukkit.featureflag.RPKProfileFeatureFlag
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import java.sql.PreparedStatement


class RPKPlayerFeatureFlagTable(database: Database, private val plugin: RPKFeatureFlagsBukkit) : Table<RPKProfileFeatureFlag>(database, RPKProfileFeatureFlag::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_profile_feature_flag(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "profile_id INTEGER," +
                            "feature_flag_id INTEGER," +
                            "enabled BOOLEAN" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.2.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "TRUNCATE rpkit_player_feature_flag"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE rpkit_player_feature_flag " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN profile_id INTEGER AFTER id"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKProfileFeatureFlag): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_player_feature_flag(profile_id, feature_flag_id, enabled) VALUES(?, ?, ?)"
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
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

    override fun update(entity: RPKProfileFeatureFlag) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_player_feature_flag SET profile_id = ?, feature_flag_id = ?, enabled = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setInt(2, entity.featureFlag.id)
                statement.setBoolean(3, entity.enabled)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKProfileFeatureFlag? {
        var profileFeatureFlag: RPKProfileFeatureFlag? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, profile_id, feature_flag_id, enabled FROM rpkit_player_feature_flag WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                    val featureFlagProvider = plugin.core.serviceManager.getServiceProvider(RPKFeatureFlagProvider::class)
                    val profile = profileProvider.getProfile(resultSet.getInt("profile_id"))
                    val featureFlag = featureFlagProvider.getFeatureFlag(resultSet.getInt("feature_flag_id"))
                    if (profile != null && featureFlag != null) {
                        profileFeatureFlag = RPKProfileFeatureFlag(
                                resultSet.getInt("id"),
                                profile,
                                featureFlag,
                                resultSet.getBoolean("enabled")
                        )
                    } else {
                        connection.prepareStatement(
                                "DELETE FROM rpkit_player_feature_flag WHERE id = ?"
                        ).use { statement ->
                            statement.setInt(1, resultSet.getInt("id"))
                            statement.executeUpdate()
                        }
                    }
                }
            }
        }
        return profileFeatureFlag
    }

    fun get(profile: RPKProfile, featureFlag: RPKFeatureFlag): RPKProfileFeatureFlag? {
        var profileFeatureFlag: RPKProfileFeatureFlag? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, profile_id, feature_flag_id, enabled FROM rpkit_player_feature_flag WHERE profile_id = ? AND feature_flag_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                statement.setInt(2, featureFlag.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    profileFeatureFlag = RPKProfileFeatureFlag(
                            resultSet.getInt("id"),
                            profile,
                            featureFlag,
                            resultSet.getBoolean("enabled")
                    )
                }
            }
        }
        return profileFeatureFlag
    }

    override fun delete(entity: RPKProfileFeatureFlag) {
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