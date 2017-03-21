package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKGitHubProfile
import com.rpkit.players.bukkit.profile.RPKGitHubProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.kohsuke.github.GHUser
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKGitHubProfileTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKGitHubProfile>(database, RPKGitHubProfile::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKGitHubProfile::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_github_profile(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "profile_id INTEGER," +
                            "name VARCHAR(256)," +
                            "oauth_token VARCHAR(1024)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKGitHubProfile): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_github_profile(profile_id, name, oauth_token) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setString(2, entity.name)
                statement.setString(3, entity.oauthToken)
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

    override fun update(entity: RPKGitHubProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_github_profile SET profile_id = ?, name = ?, oauth_token = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setString(2, entity.name)
                statement.setString(3, entity.oauthToken)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKGitHubProfile? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var githubProfile: RPKGitHubProfile? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, profile_id, name, oauth_token FROM rpkit_github_profile WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val profileId = resultSet.getInt("profile_id")
                        val profile = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class).getProfile(profileId)
                        if (profile != null) {
                            val finalGithubProfile = RPKGitHubProfileImpl(
                                    resultSet.getInt("id"),
                                    profile,
                                    resultSet.getString("name"),
                                    resultSet.getString("oauth_token")
                            )
                            cache.put(finalGithubProfile.id, finalGithubProfile)
                            githubProfile = finalGithubProfile
                        }
                    }
                }
            }
            return githubProfile
        }
    }

    fun get(profile: RPKProfile): List<RPKGitHubProfile> {
        val githubProfiles = mutableListOf<RPKGitHubProfile>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_github_profile WHERE profile_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    githubProfiles.add(get(resultSet.getInt("id"))!!)
                }
            }
        }
        return githubProfiles
    }

    fun get(user: GHUser): RPKGitHubProfile? {
        var githubProfile: RPKGitHubProfile? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_github_profile WHERE name = ?"
            ).use { statement ->
                statement.setString(1, user.name)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    githubProfile = get(resultSet.getInt("id"))
                }
            }
        }
        return githubProfile
    }

    override fun delete(entity: RPKGitHubProfile) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_github_profile WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}