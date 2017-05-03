package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldown
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKNewCharacterCooldownTable(database: Database, private val plugin: RPKCharactersBukkit): Table<RPKNewCharacterCooldown>(database, RPKNewCharacterCooldown::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKNewCharacterCooldown::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    private val profileCache = cacheManager.createCache("profileCache", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKNewCharacterCooldown::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())


    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_new_character_cooldown(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "profile_id INTEGER," +
                            "cooldown_timestamp DATE" +
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
                        "TRUNCATE TABLE rpkit_new_character_cooldown"
                ).use(PreparedStatement::executeUpdate)
                connection.prepareStatement(
                        "ALTER TABLE rpkit_new_character_cooldown " +
                                "DROP COLUMN player_id, " +
                                "ADD COLUMN profile_id INTEGER AFTER id"
                ).use(PreparedStatement::executeUpdate)
            }
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKNewCharacterCooldown): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_new_character_cooldown(profile_id, cooldown_timestamp) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setDate(2, Date(entity.cooldownTimestamp))
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    profileCache.put(entity.profile.id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKNewCharacterCooldown) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_new_character_cooldown SET profile_id = ?, cooldown_timestamp = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setDate(2, Date(entity.cooldownTimestamp))
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                profileCache.put(entity.profile.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKNewCharacterCooldown? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            var newCharacterCooldown: RPKNewCharacterCooldown? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, profile_id, cooldown_timestamp FROM rpkit_new_character_cooldown WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalNewCharacterCooldown = RPKNewCharacterCooldown(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class).getProfile(resultSet.getInt("profile_id"))!!,
                                resultSet.getDate("cooldown_timestamp").time
                        )
                        cache.put(finalNewCharacterCooldown.id, finalNewCharacterCooldown)
                        profileCache.put(finalNewCharacterCooldown.profile.id, finalNewCharacterCooldown)
                        newCharacterCooldown = finalNewCharacterCooldown
                    }
                }
            }
            return newCharacterCooldown
        }
    }

    fun get(profile: RPKProfile): RPKNewCharacterCooldown? {
        if (profileCache.containsKey(profile.id)) {
            return profileCache[profile.id]
        } else {
            var newCharacterCooldown: RPKNewCharacterCooldown? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, profile_id, cooldown_timestamp FROM rpkit_new_character_cooldown WHERE profile_id = ?"
                ).use { statement ->
                    statement.setInt(1, profile.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalNewCharacterCooldown = RPKNewCharacterCooldown(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class).getProfile(resultSet.getInt("profile_id"))!!,
                                resultSet.getDate("cooldown_timestamp").time
                        )
                        cache.put(finalNewCharacterCooldown.id, finalNewCharacterCooldown)
                        profileCache.put(finalNewCharacterCooldown.profile.id, finalNewCharacterCooldown)
                        newCharacterCooldown = finalNewCharacterCooldown
                    }
                }
            }
            return newCharacterCooldown
        }
    }

    override fun delete(entity: RPKNewCharacterCooldown) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_new_character_cooldown WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                profileCache.remove(entity.profile.id)
            }
        }
    }

}