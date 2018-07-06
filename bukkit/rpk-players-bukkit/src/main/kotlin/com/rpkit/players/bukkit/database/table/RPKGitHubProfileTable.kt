package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.rpkit.Tables.RPKIT_GITHUB_PROFILE
import com.rpkit.players.bukkit.profile.RPKGitHubProfile
import com.rpkit.players.bukkit.profile.RPKGitHubProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import org.kohsuke.github.GHUser


class RPKGitHubProfileTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKGitHubProfile>(database, RPKGitHubProfile::class) {

    private val cache = database.cacheManager.createCache("rpk-players-bukkit.rpkit_github_profile.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKGitHubProfile::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_GITHUB_PROFILE)
                .column(RPKIT_GITHUB_PROFILE.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_GITHUB_PROFILE.PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_GITHUB_PROFILE.NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN, SQLDataType.VARCHAR(1024))
                .constraints(
                        constraint("pk_rpkit_github_profile").primaryKey(RPKIT_GITHUB_PROFILE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKGitHubProfile): Int {
        database.create
                .insertInto(
                        RPKIT_GITHUB_PROFILE,
                        RPKIT_GITHUB_PROFILE.PROFILE_ID,
                        RPKIT_GITHUB_PROFILE.NAME,
                        RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                )
                .values(
                        entity.profile.id,
                        entity.name,
                        entity.oauthToken
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKGitHubProfile) {
        database.create
                .update(RPKIT_GITHUB_PROFILE)
                .set(RPKIT_GITHUB_PROFILE.PROFILE_ID, entity.profile.id)
                .set(RPKIT_GITHUB_PROFILE.NAME, entity.name)
                .set(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN, entity.oauthToken)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKGitHubProfile? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_GITHUB_PROFILE.PROFILE_ID,
                            RPKIT_GITHUB_PROFILE.NAME,
                            RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                    )
                    .from(RPKIT_GITHUB_PROFILE)
                    .where(RPKIT_GITHUB_PROFILE.ID.eq(id))
                    .fetchOne() ?: return null
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profileId = result.get(RPKIT_GITHUB_PROFILE.PROFILE_ID)
            val profile = profileProvider.getProfile(profileId)
            if (profile != null) {
                val githubProfile = RPKGitHubProfileImpl(
                        id,
                        profile,
                        result.get(RPKIT_GITHUB_PROFILE.NAME),
                        result.get(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN)
                )
                cache.put(githubProfile.id, githubProfile)
                return githubProfile
            } else {
                database.create
                        .deleteFrom(RPKIT_GITHUB_PROFILE)
                        .where(RPKIT_GITHUB_PROFILE.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(profile: RPKProfile): List<RPKGitHubProfile> {
        val results = database.create
                .select(RPKIT_GITHUB_PROFILE.ID)
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.PROFILE_ID.eq(profile.id))
                .fetch()
        val githubProfiles = results.map { result ->
            get(result.get(RPKIT_GITHUB_PROFILE.ID))
        }.filterNotNull()
        return githubProfiles
    }

    fun get(user: GHUser): RPKGitHubProfile? {
        val result = database.create
                .select(RPKIT_GITHUB_PROFILE.ID)
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.NAME.eq(user.name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_GITHUB_PROFILE.ID))
    }

    override fun delete(entity: RPKGitHubProfile) {
        database.create
                .deleteFrom(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}