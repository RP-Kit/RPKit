package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileToken
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileTokenImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKMinecraftProfileTokenTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKMinecraftProfileToken>(database, RPKMinecraftProfileToken::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKMinecraftProfileToken::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_minecraft_profile_token(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "minecraft_profile_id INTEGER," +
                            "token VARCHAR(36)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKMinecraftProfileToken): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_minecraft_profile_token(minecraft_profile_id, token) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setString(2, entity.token)
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

    override fun update(entity: RPKMinecraftProfileToken) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_minecraft_profile_token SET minecraft_profile_id = ?, token = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.minecraftProfile.id)
                statement.setString(2, entity.token)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKMinecraftProfileToken? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var minecraftProfileToken: RPKMinecraftProfileToken? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, minecraft_profile_id, token FROM rpkit_minecraft_profile_token WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val minecraftProfileId = resultSet.getInt("minecraft_profile_id")
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
                        if (minecraftProfile != null) {
                            val finalMinecraftProfileToken = RPKMinecraftProfileTokenImpl(
                                    resultSet.getInt("id"),
                                    minecraftProfile,
                                    resultSet.getString("token")
                            )
                            cache.put(finalMinecraftProfileToken.id, finalMinecraftProfileToken)
                            minecraftProfileToken = finalMinecraftProfileToken
                        } else {
                            connection.prepareStatement(
                                    "DELETE FROM rpkit_minecraft_profile_token WHERE id = ?"
                            ).use { statement ->
                                statement.setInt(1, id)
                                statement.executeUpdate()
                                cache.remove(id)
                            }
                        }
                    }
                }
            }
            return minecraftProfileToken
        }
    }

    fun get(profile: RPKMinecraftProfile): RPKMinecraftProfileToken? {
        var minecraftProfileToken: RPKMinecraftProfileToken? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_minecraft_profile_token WHERE minecraft_profile_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    minecraftProfileToken = get(resultSet.getInt("id"))
                }
            }
        }
        return minecraftProfileToken
    }

    override fun delete(entity: RPKMinecraftProfileToken) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_minecraft_profile_token WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}