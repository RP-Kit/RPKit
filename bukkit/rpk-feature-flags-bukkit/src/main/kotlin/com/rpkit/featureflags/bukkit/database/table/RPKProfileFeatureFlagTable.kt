package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.jooq.rpkit.Tables.RPKIT_PROFILE_FEATURE_FLAG
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagProvider
import com.rpkit.featureflags.bukkit.featureflag.RPKProfileFeatureFlag
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKProfileFeatureFlagTable(database: Database, private val plugin: RPKFeatureFlagsBukkit) : Table<RPKProfileFeatureFlag>(database, RPKProfileFeatureFlag::class) {

    private val cache = database.cacheManager.createCache("rpk-feature-flags-bukkit.rpkit_profile_feature_flag.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfileFeatureFlag::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers * 50L)))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PROFILE_FEATURE_FLAG)
                .column(RPKIT_PROFILE_FEATURE_FLAG.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_ID, SQLDataType.INTEGER)
                .column(RPKIT_PROFILE_FEATURE_FLAG.ENABLED, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_profile_feature_flag").primaryKey(RPKIT_PROFILE_FEATURE_FLAG.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.create
                    .dropTableIfExists(table("rpkit_player_feature_flag"))
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKProfileFeatureFlag): Int {
        database.create
                .insertInto(
                        RPKIT_PROFILE_FEATURE_FLAG,
                        RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID,
                        RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_ID,
                        RPKIT_PROFILE_FEATURE_FLAG.ENABLED
                )
                .values(
                        entity.profile.id,
                        entity.featureFlag.id,
                        if (entity.enabled) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKProfileFeatureFlag) {
        database.create
                .update(RPKIT_PROFILE_FEATURE_FLAG)
                .set(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID, entity.profile.id)
                .set(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_ID, entity.featureFlag.id)
                .set(RPKIT_PROFILE_FEATURE_FLAG.ENABLED, if (entity.enabled) 1.toByte() else 0.toByte())
                .where(RPKIT_PROFILE_FEATURE_FLAG.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKProfileFeatureFlag? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID,
                            RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_ID,
                            RPKIT_PROFILE_FEATURE_FLAG.ENABLED
                    )
                    .from(RPKIT_PROFILE_FEATURE_FLAG)
                    .where(RPKIT_PROFILE_FEATURE_FLAG.ID.eq(id))
                    .fetchOne() ?: return null
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profileId = result.get(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID)
            val profile = profileProvider.getProfile(profileId)
            val featureFlagProvider = plugin.core.serviceManager.getServiceProvider(RPKFeatureFlagProvider::class)
            val featureFlagId = result.get(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_ID)
            val featureFlag = featureFlagProvider.getFeatureFlag(featureFlagId)
            if (profile != null && featureFlag != null) {
                val profileFeatureFlag = RPKProfileFeatureFlag(
                        id,
                        profile,
                        featureFlag,
                        result.get(RPKIT_PROFILE_FEATURE_FLAG.ENABLED) == 1.toByte()
                )
                cache.put(id, profileFeatureFlag)
                return profileFeatureFlag
            } else {
                database.create
                        .deleteFrom(RPKIT_PROFILE_FEATURE_FLAG)
                        .where(RPKIT_PROFILE_FEATURE_FLAG.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    fun get(profile: RPKProfile, featureFlag: RPKFeatureFlag): RPKProfileFeatureFlag? {
        val result = database.create
                .select(RPKIT_PROFILE_FEATURE_FLAG.ID)
                .from(RPKIT_PROFILE_FEATURE_FLAG)
                .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(profile.id))
                .and(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_ID.eq(featureFlag.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_PROFILE_FEATURE_FLAG.ID])
    }

    override fun delete(entity: RPKProfileFeatureFlag) {
        database.create
                .deleteFrom(RPKIT_PROFILE_FEATURE_FLAG)
                .where(RPKIT_PROFILE_FEATURE_FLAG.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}