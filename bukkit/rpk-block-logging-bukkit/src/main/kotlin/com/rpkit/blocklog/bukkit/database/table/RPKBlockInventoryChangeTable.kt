package com.rpkit.blocklog.bukkit.database.table

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistory
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChange
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChangeImpl
import com.rpkit.blocklog.bukkit.database.jooq.rpkit.Tables.RPKIT_BLOCK_INVENTORY_CHANGE
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.bukkit.util.toItemStackArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import java.sql.Timestamp


class RPKBlockInventoryChangeTable(database: Database, private val plugin: RPKBlockLoggingBukkit): Table<RPKBlockInventoryChange>(database, RPKBlockInventoryChange::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_block_inventory_change.id.enabled")) {
        database.cacheManager.createCache("rpk-block-logging-bukkit.rpkit_block_inventory_change.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBlockInventoryChange::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_block_inventory_change.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_BLOCK_INVENTORY_CHANGE)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID, SQLDataType.INTEGER)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.TIME, SQLDataType.TIMESTAMP)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.FROM, SQLDataType.BLOB)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.TO, SQLDataType.BLOB)
                .column(RPKIT_BLOCK_INVENTORY_CHANGE.REASON, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_block_inventory_change").primaryKey(RPKIT_BLOCK_INVENTORY_CHANGE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.4.0")
        }
    }

    override fun insert(entity: RPKBlockInventoryChange): Int {
        database.create
                .insertInto(
                        RPKIT_BLOCK_INVENTORY_CHANGE,
                        RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.TIME,
                        RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.FROM,
                        RPKIT_BLOCK_INVENTORY_CHANGE.TO,
                        RPKIT_BLOCK_INVENTORY_CHANGE.REASON
                )
                .values(
                        entity.blockHistory.id,
                        Timestamp(entity.time),
                        entity.profile?.id,
                        entity.minecraftProfile?.id,
                        entity.character?.id,
                        entity.from.toByteArray(),
                        entity.to.toByteArray(),
                        entity.reason
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKBlockInventoryChange) {
        database.create
                .update(RPKIT_BLOCK_INVENTORY_CHANGE)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID, entity.blockHistory.id)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.TIME, Timestamp(entity.time))
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID, entity.profile?.id)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID, entity.minecraftProfile?.id)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID, entity.character?.id)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.FROM, entity.from.toByteArray())
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.TO, entity.to.toByteArray())
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.REASON, entity.reason)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKBlockInventoryChange? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID,
                            RPKIT_BLOCK_INVENTORY_CHANGE.TIME,
                            RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID,
                            RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID,
                            RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID,
                            RPKIT_BLOCK_INVENTORY_CHANGE.FROM,
                            RPKIT_BLOCK_INVENTORY_CHANGE.TO,
                            RPKIT_BLOCK_INVENTORY_CHANGE.REASON
                    )
                    .from(RPKIT_BLOCK_INVENTORY_CHANGE)
                    .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(id))
                    .fetchOne() ?: return null
            val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
            val blockHistoryId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID)
            val blockHistory = blockHistoryProvider.getBlockHistory(blockHistoryId)
            if (blockHistory == null) {
                database.create
                        .deleteFrom(RPKIT_BLOCK_INVENTORY_CHANGE)
                        .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profileId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID)
            val profile = if (profileId == null) null else profileProvider.getProfile(profileId)
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID)
            val minecraftProfile = if (minecraftProfileId == null) null else minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID)
            val character = if (characterId == null) null else characterProvider.getCharacter(characterId)
            val blockInventoryChange = RPKBlockInventoryChangeImpl(
                    id,
                    blockHistory,
                    result.get(RPKIT_BLOCK_INVENTORY_CHANGE.TIME).time,
                    profile,
                    minecraftProfile,
                    character,
                    result.get(RPKIT_BLOCK_INVENTORY_CHANGE.FROM).toItemStackArray(),
                    result.get(RPKIT_BLOCK_INVENTORY_CHANGE.TO).toItemStackArray(),
                    result.get(RPKIT_BLOCK_INVENTORY_CHANGE.REASON)
            )
            cache?.put(id, blockInventoryChange)
            return blockInventoryChange
        }
    }

    fun get(blockHistory: RPKBlockHistory): List<RPKBlockInventoryChange> {
        val results = database.create
                .select(RPKIT_BLOCK_INVENTORY_CHANGE.ID)
                .from(RPKIT_BLOCK_INVENTORY_CHANGE)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID.eq(blockHistory.id))
                .fetch()
        return results
                .map { result -> get(result[RPKIT_BLOCK_INVENTORY_CHANGE.ID]) }
                .filterNotNull()
    }

    override fun delete(entity: RPKBlockInventoryChange) {
        database.create
                .deleteFrom(RPKIT_BLOCK_INVENTORY_CHANGE)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}