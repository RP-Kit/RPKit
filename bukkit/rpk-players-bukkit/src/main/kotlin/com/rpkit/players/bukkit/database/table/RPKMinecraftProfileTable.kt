package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.OfflinePlayer
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.INTEGER
import java.util.*


class RPKMinecraftProfileTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKMinecraftProfile>(database, RPKMinecraftProfile::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKMinecraftProfile::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_minecraft_profile(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "profile_id INTEGER," +
                            "minecraft_uuid VARCHAR(36)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKMinecraftProfile): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_minecraft_profile(profile_id, minecraft_uuid) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                val profile = entity.profile
                if (profile == null) {
                    statement.setNull(1, INTEGER)
                } else {
                    statement.setInt(1, profile.id)
                }
                statement.setString(2, entity.minecraftUUID.toString())
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

    override fun update(entity: RPKMinecraftProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_minecraft_profile SET profile_id = ?, minecraft_uuid = ? WHERE id = ?"
            ).use { statement ->
                val profile = entity.profile
                if (profile == null) {
                    statement.setNull(1, INTEGER)
                } else {
                    statement.setInt(1, profile.id)
                }
                statement.setString(2, entity.minecraftUUID.toString())
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKMinecraftProfile? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var minecraftProfile: RPKMinecraftProfile? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, profile_id, minecraft_uuid FROM rpkit_minecraft_profile WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val profileId = resultSet.getInt("profile_id")
                        if (profileId == 0) {
                            val finalMinecraftProfile = RPKMinecraftProfileImpl(
                                    resultSet.getInt("id"),
                                    null,
                                    UUID.fromString(resultSet.getString("minecraft_uuid"))
                            )
                            minecraftProfile = finalMinecraftProfile
                            cache.put(finalMinecraftProfile.id, finalMinecraftProfile)
                        } else {
                            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                            val finalMinecraftProfile = RPKMinecraftProfileImpl(
                                    resultSet.getInt("id"),
                                    profileProvider.getProfile(resultSet.getInt("profile_id")),
                                    UUID.fromString(resultSet.getString("minecraft_uuid"))
                            )
                            minecraftProfile = finalMinecraftProfile
                            cache.put(finalMinecraftProfile.id, finalMinecraftProfile)
                        }
                    }
                }
            }
            return minecraftProfile
        }
    }

    fun get(profile: RPKProfile): List<RPKMinecraftProfile> {
        val minecraftProfiles = mutableListOf<RPKMinecraftProfile>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_minecraft_profile WHERE profile_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    minecraftProfiles.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return minecraftProfiles
    }

    fun get(player: OfflinePlayer): RPKMinecraftProfile? {
        var minecraftProfile: RPKMinecraftProfile? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_minecraft_profile WHERE minecraft_uuid = ?"
            ).use { statement ->
                statement.setString(1, player.uniqueId.toString())
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    minecraftProfile = get(resultSet.getInt("id"))
                }
            }
        }
        return minecraftProfile
    }

    override fun delete(entity: RPKMinecraftProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_minecraft_profile WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }
}