package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKProfileTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKProfile>(database, RPKProfile::class) {

    private val cacheManger = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManger.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfile::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_profile(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(16) UNIQUE," +
                            "password_hash BLOB," +
                            "password_salt BLOB" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKProfile): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_profile(name, password_hash, password_salt) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setBytes(2, entity.passwordHash)
                statement.setBytes(3, entity.passwordSalt)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_profile SET name = ?, password_hash = ?, password_salt = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, entity.name)
                statement.setBytes(2, entity.passwordHash)
                statement.setBytes(3, entity.passwordSalt)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKProfile? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var profile: RPKProfile? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, password_hash, password_salt FROM rpkit_profile WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalProfile = RPKProfileImpl(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getBytes("password_hash"),
                                resultSet.getBytes("password_salt")
                        )
                        cache.put(finalProfile.id, finalProfile)
                        profile = finalProfile
                    }
                }
            }
            return profile
        }
    }

    fun get(name: String): RPKProfile? {
        var profile: RPKProfile? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_profile WHERE name = ?"
            ).use { statement ->
                statement.setString(1, name)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    profile = get(resultSet.getInt("id"))
                }
            }
        }
        return profile
    }

    override fun delete(entity: RPKProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_profile WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}