package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.jooq.rpkit.Tables.RPKIT_PLAYER_GETTING_KEY
import com.rpkit.locks.bukkit.lock.RPKPlayerGettingKey
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKPlayerGettingKeyTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKPlayerGettingKey>(database, RPKPlayerGettingKey::class) {

    private val cache = database.cacheManager.createCache("rpk-locks-bukkit.rpkit_player_getting_key.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPlayerGettingKey::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PLAYER_GETTING_KEY)
                .column(RPKIT_PLAYER_GETTING_KEY.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_player_getting_key").primaryKey(RPKIT_PLAYER_GETTING_KEY.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.create
                    .truncate(RPKIT_PLAYER_GETTING_KEY)
                    .execute()
            database.create
                    .alterTable(RPKIT_PLAYER_GETTING_KEY)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_PLAYER_GETTING_KEY)
                    .addColumn(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKPlayerGettingKey): Int {
        database.create
                .insertInto(
                        RPKIT_PLAYER_GETTING_KEY,
                        RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID
                )
                .values(entity.minecraftProfile.id)
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKPlayerGettingKey) {
        database.create
                .update(RPKIT_PLAYER_GETTING_KEY)
                .set(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .where(RPKIT_PLAYER_GETTING_KEY.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKPlayerGettingKey? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_PLAYER_GETTING_KEY)
                    .where(RPKIT_PLAYER_GETTING_KEY.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            if (minecraftProfile != null) {
                val playerGettingKey = RPKPlayerGettingKey(
                        id,
                        minecraftProfile
                )
                cache.put(id, playerGettingKey)
                return playerGettingKey
            } else {
                database.create
                        .deleteFrom(RPKIT_PLAYER_GETTING_KEY)
                        .where(RPKIT_PLAYER_GETTING_KEY.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKPlayerGettingKey? {
        val result = database.create
                .select(RPKIT_PLAYER_GETTING_KEY.ID)
                .from(RPKIT_PLAYER_GETTING_KEY)
                .where(RPKIT_PLAYER_GETTING_KEY.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_PLAYER_GETTING_KEY.ID])
    }

    override fun delete(entity: RPKPlayerGettingKey) {
        database.create
                .deleteFrom(RPKIT_PLAYER_GETTING_KEY)
                .where(RPKIT_PLAYER_GETTING_KEY.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }
    
}