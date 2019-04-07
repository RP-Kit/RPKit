package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.jooq.rpkit.Tables.RPKIT_PLAYER_UNCLAIMING
import com.rpkit.locks.bukkit.lock.RPKPlayerUnclaiming
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType


class RPKPlayerUnclaimingTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKPlayerUnclaiming>(database, RPKPlayerUnclaiming::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_player_unclaiming.id.enabled")) {
        database.cacheManager.createCache("rpk-locks-bukkit.rpkit_player_unclaiming.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPlayerUnclaiming::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_player_unclaiming.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PLAYER_UNCLAIMING)
                .column(RPKIT_PLAYER_UNCLAIMING.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_player_unclaiming").primaryKey(RPKIT_PLAYER_UNCLAIMING.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.create
                    .truncate(RPKIT_PLAYER_UNCLAIMING)
                    .execute()
            database.create
                    .alterTable(RPKIT_PLAYER_UNCLAIMING)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_PLAYER_UNCLAIMING)
                    .addColumn(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKPlayerUnclaiming): Int {
        database.create
                .insertInto(
                        RPKIT_PLAYER_UNCLAIMING,
                        RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.minecraftProfile.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKPlayerUnclaiming) {
        database.create
                .update(RPKIT_PLAYER_UNCLAIMING)
                .set(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .where(RPKIT_PLAYER_UNCLAIMING.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKPlayerUnclaiming? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_PLAYER_UNCLAIMING)
                    .where(RPKIT_PLAYER_UNCLAIMING.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            return if (minecraftProfile != null) {
                val playerUnclaiming = RPKPlayerUnclaiming(
                        id,
                        minecraftProfile
                )
                cache?.put(id, playerUnclaiming)
                playerUnclaiming
            } else {
                database.create
                        .deleteFrom(RPKIT_PLAYER_UNCLAIMING)
                        .where(RPKIT_PLAYER_UNCLAIMING.ID.eq(id))
                        .execute()
                cache?.remove(id)
                null
            }
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKPlayerUnclaiming? {
        val result = database.create
                .select(RPKIT_PLAYER_UNCLAIMING.ID)
                .from(RPKIT_PLAYER_UNCLAIMING)
                .where(RPKIT_PLAYER_UNCLAIMING.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_PLAYER_UNCLAIMING.ID])
    }

    override fun delete(entity: RPKPlayerUnclaiming) {
        database.create
                .deleteFrom(RPKIT_PLAYER_UNCLAIMING)
                .where(RPKIT_PLAYER_UNCLAIMING.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}