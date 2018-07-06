package com.rpkit.essentials.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.jooq.rpkit.Tables.RPKIT_TRACKING_ENABLED
import com.rpkit.essentials.bukkit.tracking.RPKTrackingEnabled
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKTrackingEnabledTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKTrackingEnabled>(database, RPKTrackingEnabled::class) {

    private val cache = database.cacheManager.createCache("rpk-essentials-bukkit.rpkit_tracking_enabled.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKTrackingEnabled::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_TRACKING_ENABLED)
                .column(RPKIT_TRACKING_ENABLED.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_TRACKING_ENABLED.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_TRACKING_ENABLED.ENABLED, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_tracking_enabled").primaryKey(RPKIT_TRACKING_ENABLED.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKTrackingEnabled): Int {
        database.create
                .insertInto(
                        RPKIT_TRACKING_ENABLED,
                        RPKIT_TRACKING_ENABLED.CHARACTER_ID,
                        RPKIT_TRACKING_ENABLED.ENABLED
                )
                .values(
                        entity.character.id,
                        if (entity.enabled) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKTrackingEnabled) {
        database.create
                .update(RPKIT_TRACKING_ENABLED)
                .set(RPKIT_TRACKING_ENABLED.CHARACTER_ID, entity.character.id)
                .set(RPKIT_TRACKING_ENABLED.ENABLED, if (entity.enabled) 1.toByte() else 0.toByte())
                .where(RPKIT_TRACKING_ENABLED.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKTrackingEnabled? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_TRACKING_ENABLED.CHARACTER_ID,
                            RPKIT_TRACKING_ENABLED.ENABLED
                    )
                    .from(RPKIT_TRACKING_ENABLED)
                    .where(RPKIT_TRACKING_ENABLED.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_TRACKING_ENABLED.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (character != null) {
                val trackingEnabled = RPKTrackingEnabled(
                        id,
                        character,
                        result.get(RPKIT_TRACKING_ENABLED.ENABLED) == 1.toByte()
                )
                cache.put(id, trackingEnabled)
                return trackingEnabled
            } else {
                database.create
                        .deleteFrom(RPKIT_TRACKING_ENABLED)
                        .where(RPKIT_TRACKING_ENABLED.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    fun get(character: RPKCharacter): RPKTrackingEnabled? {
        val result = database.create
                .select(RPKIT_TRACKING_ENABLED.ID)
                .from(RPKIT_TRACKING_ENABLED)
                .where(RPKIT_TRACKING_ENABLED.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_TRACKING_ENABLED.ID))
    }

    override fun delete(entity: RPKTrackingEnabled) {
        database.create
                .deleteFrom(RPKIT_TRACKING_ENABLED)
                .where(RPKIT_TRACKING_ENABLED.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }
}