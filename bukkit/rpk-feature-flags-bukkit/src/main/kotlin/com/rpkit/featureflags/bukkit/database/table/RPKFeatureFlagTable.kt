package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.jooq.rpkit.Tables.RPKIT_FEATURE_FLAG
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKFeatureFlagTable(database: Database, private val plugin: RPKFeatureFlagsBukkit): Table<RPKFeatureFlag>(database, RPKFeatureFlag::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_feature_flag.id.enabled")) {
        database.cacheManager.createCache("rpk-feature-flags-bukkit.rpkit_feature_flag.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKFeatureFlag::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_feature_flag.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_FEATURE_FLAG)
                .column(RPKIT_FEATURE_FLAG.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_FEATURE_FLAG.NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_FEATURE_FLAG.ENABLED_BY_DEFAULT, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_feature_flag").primaryKey(RPKIT_FEATURE_FLAG.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKFeatureFlag): Int {
        database.create
                .insertInto(
                        RPKIT_FEATURE_FLAG,
                        RPKIT_FEATURE_FLAG.NAME,
                        RPKIT_FEATURE_FLAG.ENABLED_BY_DEFAULT
                )
                .values(
                        entity.name,
                        if (entity.isEnabledByDefault) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKFeatureFlag) {
        database.create
                .update(RPKIT_FEATURE_FLAG)
                .set(RPKIT_FEATURE_FLAG.NAME, entity.name)
                .set(RPKIT_FEATURE_FLAG.ENABLED_BY_DEFAULT, if (entity.isEnabledByDefault) 1.toByte() else 0.toByte())
                .where(RPKIT_FEATURE_FLAG.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKFeatureFlag? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_FEATURE_FLAG.NAME,
                            RPKIT_FEATURE_FLAG.ENABLED_BY_DEFAULT
                    )
                    .from(RPKIT_FEATURE_FLAG)
                    .where(RPKIT_FEATURE_FLAG.ID.eq(id))
                    .fetchOne() ?: return null
            val featureFlag = RPKFeatureFlagImpl(
                    plugin,
                    id,
                    result.get(RPKIT_FEATURE_FLAG.NAME),
                    result.get(RPKIT_FEATURE_FLAG.ENABLED_BY_DEFAULT) == 1.toByte()
            )
            cache?.put(id, featureFlag)
            return featureFlag
        }
    }

    fun get(name: String): RPKFeatureFlag? {
        val result = database.create
                .select(RPKIT_FEATURE_FLAG.ID)
                .from(RPKIT_FEATURE_FLAG)
                .where(RPKIT_FEATURE_FLAG.NAME.eq(name))
                .fetchOne() ?: return null
        return get(result[RPKIT_FEATURE_FLAG.ID])
    }

    override fun delete(entity: RPKFeatureFlag) {
        database.create
                .deleteFrom(RPKIT_FEATURE_FLAG)
                .where(RPKIT_FEATURE_FLAG.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}