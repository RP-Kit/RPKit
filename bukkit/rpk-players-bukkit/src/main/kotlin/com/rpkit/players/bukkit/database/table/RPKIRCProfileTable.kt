package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKIRCProfile
import com.rpkit.players.bukkit.profile.RPKIRCProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.pircbotx.User
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKIRCProfileTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKIRCProfile>(database, RPKIRCProfile::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKIRCProfile::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_irc_profile(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "profile_id INTEGER," +
                            "nick VARCHAR(256)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKIRCProfile): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_irc_profile(profile_id, nick) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setString(2, entity.nick)
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

    override fun update(entity: RPKIRCProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_irc_profile SET profile_id = ?, nick = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setString(2, entity.nick)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKIRCProfile? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var ircProfile: RPKIRCProfile? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, profile_id, nick FROM rpkit_irc_profile WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val profileId = resultSet.getInt("profile_id")
                        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                        val profile = profileProvider.getProfile(profileId)
                        if (profile != null) {
                            val finalIrcProfile = RPKIRCProfileImpl(
                                    resultSet.getInt("id"),
                                    profile,
                                    resultSet.getString("nick")
                            )
                            cache.put(finalIrcProfile.id, finalIrcProfile)
                            ircProfile = finalIrcProfile
                        }
                    }
                }
            }
            return ircProfile
        }
    }

    fun get(profile: RPKProfile): List<RPKIRCProfile> {
        val ircProfiles = mutableListOf<RPKIRCProfile>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_irc_profile WHERE profile_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val ircProfile = get(resultSet.getInt("id"))
                    if (ircProfile != null) {
                        ircProfiles.add(ircProfile)
                    }
                }
            }
        }
        return ircProfiles
    }

    fun get(user: User): RPKIRCProfile? {
        var ircProfile: RPKIRCProfile? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_irc_profile WHERE nick = ?"
            ).use { statement ->
                statement.setString(1, user.nick)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    ircProfile = get(resultSet.getInt("id"))
                }
            }
        }
        return ircProfile
    }

    override fun delete(entity: RPKIRCProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_irc_profile WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}