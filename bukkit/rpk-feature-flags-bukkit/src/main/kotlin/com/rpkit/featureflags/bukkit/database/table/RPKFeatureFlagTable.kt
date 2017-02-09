package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagImpl
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKFeatureFlagTable(database: Database, private val plugin: RPKFeatureFlagsBukkit): Table<RPKFeatureFlag>(database, RPKFeatureFlag::class) {
    override fun create() {
        database.createConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS rpkit_feature_flag(" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(256)," +
                        "enabled_by_default BOOLEAN" +
                ")"
        ).use(PreparedStatement::executeUpdate)
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKFeatureFlag): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_feature_flag(name, enabled_by_default) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setBoolean(2, entity.isEnabledByDefault)
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

    override fun update(entity: RPKFeatureFlag) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_feature_flag SET name = ?, enabled_by_default = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setBoolean(2, entity.isEnabledByDefault)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKFeatureFlag? {
        var featureFlag: RPKFeatureFlag? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, name, enabled_by_default FROM rpkit_feature_flag WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    featureFlag = RPKFeatureFlagImpl(
                            plugin,
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getBoolean("enabled_by_default")
                    )
                }
            }
        }
        return featureFlag
    }

    fun get(name: String): RPKFeatureFlag? {
        var featureFlag: RPKFeatureFlag? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, name, enabled_by_default FROM rpkit_feature_flag WHERE name = ?"
            ).use { statement ->
                statement.setString(1, name)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    featureFlag = RPKFeatureFlagImpl(
                            plugin,
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getBoolean("enabled_by_default")
                    )
                }
            }
        }
        return featureFlag
    }

    override fun delete(entity: RPKFeatureFlag) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_feature_flag WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }
}