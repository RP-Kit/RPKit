package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.jooq.rpkit.Tables.RPKIT_LOG_MESSAGES_ENABLED
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessagesEnabled
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType


class RPKLogMessagesEnabledTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKLogMessagesEnabled>(database, RPKLogMessagesEnabled::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_log_messages_enabled.id.enabled")) {
        database.cacheManager.createCache("rpk-essentials-bukkit.rpkit_log_messages_enabled.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKLogMessagesEnabled::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_log_messages_enabled.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_LOG_MESSAGES_ENABLED)
                .column(RPKIT_LOG_MESSAGES_ENABLED.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_LOG_MESSAGES_ENABLED.ENABLED, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_log_messages_enabled").primaryKey(RPKIT_LOG_MESSAGES_ENABLED.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.create
                    .truncate(RPKIT_LOG_MESSAGES_ENABLED)
                    .execute()
            database.create
                    .alterTable(RPKIT_LOG_MESSAGES_ENABLED)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_LOG_MESSAGES_ENABLED)
                    .addColumn(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKLogMessagesEnabled): Int {
        database.create
                .insertInto(
                        RPKIT_LOG_MESSAGES_ENABLED,
                        RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID,
                        RPKIT_LOG_MESSAGES_ENABLED.ENABLED
                )
                .values(
                        entity.minecraftProfile.id,
                        if (entity.enabled) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKLogMessagesEnabled) {
        database.create
                .update(RPKIT_LOG_MESSAGES_ENABLED)
                .set(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(RPKIT_LOG_MESSAGES_ENABLED.ENABLED, if (entity.enabled) 1.toByte() else 0.toByte())
                .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKLogMessagesEnabled? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID,
                            RPKIT_LOG_MESSAGES_ENABLED.ENABLED
                    )
                    .from(RPKIT_LOG_MESSAGES_ENABLED)
                    .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            if (minecraftProfile != null) {
                val logMessagesEnabled = RPKLogMessagesEnabled(
                        id,
                        minecraftProfile,
                        result.get(RPKIT_LOG_MESSAGES_ENABLED.ENABLED) == 1.toByte()
                )
                cache?.put(id, logMessagesEnabled)
                return logMessagesEnabled
            } else {
                database.create
                        .deleteFrom(RPKIT_LOG_MESSAGES_ENABLED)
                        .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKLogMessagesEnabled? {
        val result = database.create
                .select(RPKIT_LOG_MESSAGES_ENABLED.ID)
                .from(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_LOG_MESSAGES_ENABLED.ID])
    }

    override fun delete(entity: RPKLogMessagesEnabled) {
        database.create
                .deleteFrom(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}