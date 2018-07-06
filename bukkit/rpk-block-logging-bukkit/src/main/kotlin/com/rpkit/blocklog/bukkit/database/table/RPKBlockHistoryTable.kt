package com.rpkit.blocklog.bukkit.database.table

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistory
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryImpl
import com.rpkit.blocklog.bukkit.database.jooq.rpkit.Tables.RPKIT_BLOCK_HISTORY
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.bukkit.block.Block
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKBlockHistoryTable(database: Database, private val plugin: RPKBlockLoggingBukkit): Table<RPKBlockHistory>(database, RPKBlockHistory::class) {

    private val cache = database.cacheManager.createCache("rpk-block-logging-bukkit.rpkit_block_history.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBlockHistory::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers * 10L)))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_BLOCK_HISTORY)
                .column(RPKIT_BLOCK_HISTORY.ID,
                        if (database.dialect == SQLDialect.SQLITE)
                            SQLiteDataType.INTEGER.identity(true)
                        else
                            SQLDataType.INTEGER.identity(true)
                )
                .column(RPKIT_BLOCK_HISTORY.WORLD, SQLDataType.VARCHAR(256))
                .column(RPKIT_BLOCK_HISTORY.X, SQLDataType.INTEGER)
                .column(RPKIT_BLOCK_HISTORY.Y, SQLDataType.INTEGER)
                .column(RPKIT_BLOCK_HISTORY.Z, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_block_history").primaryKey(RPKIT_BLOCK_HISTORY.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.4.0")
        }
    }

    override fun insert(entity: RPKBlockHistory): Int {
        database.create
                .insertInto(
                        RPKIT_BLOCK_HISTORY,
                        RPKIT_BLOCK_HISTORY.WORLD,
                        RPKIT_BLOCK_HISTORY.X,
                        RPKIT_BLOCK_HISTORY.Y,
                        RPKIT_BLOCK_HISTORY.Z
                )
                .values(
                        entity.world.name,
                        entity.x,
                        entity.y,
                        entity.z
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKBlockHistory) {
        database.create
                .update(RPKIT_BLOCK_HISTORY)
                .set(RPKIT_BLOCK_HISTORY.WORLD, entity.world.name)
                .set(RPKIT_BLOCK_HISTORY.X, entity.x)
                .set(RPKIT_BLOCK_HISTORY.Y, entity.y)
                .set(RPKIT_BLOCK_HISTORY.Z, entity.z)
                .where(RPKIT_BLOCK_HISTORY.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKBlockHistory? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_BLOCK_HISTORY.WORLD,
                            RPKIT_BLOCK_HISTORY.X,
                            RPKIT_BLOCK_HISTORY.Y,
                            RPKIT_BLOCK_HISTORY.Z
                    )
                    .from(RPKIT_BLOCK_HISTORY)
                    .where(RPKIT_BLOCK_HISTORY.ID.eq(id))
                    .fetchOne()
            val blockHistory = RPKBlockHistoryImpl(
                    plugin,
                    id,
                    plugin.server.getWorld(result.get(RPKIT_BLOCK_HISTORY.WORLD)),
                    result.get(RPKIT_BLOCK_HISTORY.X),
                    result.get(RPKIT_BLOCK_HISTORY.Y),
                    result.get(RPKIT_BLOCK_HISTORY.Z)
            )
            cache.put(id, blockHistory)
            return blockHistory
        }
    }

    fun get(block: Block): RPKBlockHistory? {
        val result = database.create
                .select(RPKIT_BLOCK_HISTORY.ID)
                .from(RPKIT_BLOCK_HISTORY)
                .where(RPKIT_BLOCK_HISTORY.WORLD.eq(block.world.name))
                .and(RPKIT_BLOCK_HISTORY.X.eq(block.x))
                .and(RPKIT_BLOCK_HISTORY.Y.eq(block.y))
                .and(RPKIT_BLOCK_HISTORY.Z.eq(block.z))
                .fetchOne() ?: return null
        val id = result.get(RPKIT_BLOCK_HISTORY.ID)
        if (id == null) {
            return null
        } else {
            return get(id)
        }
    }

    override fun delete(entity: RPKBlockHistory) {
        database.create
                .deleteFrom(RPKIT_BLOCK_HISTORY)
                .where(RPKIT_BLOCK_HISTORY.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}