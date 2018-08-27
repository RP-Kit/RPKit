package com.rpkit.travel.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.jooq.rpkit.Tables.RPKIT_WARP
import com.rpkit.travel.bukkit.warp.RPKWarpImpl
import com.rpkit.warp.bukkit.warp.RPKWarp
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKWarpTable(database: Database, private val plugin: RPKTravelBukkit): Table<RPKWarp>(database, RPKWarp::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_warp.id.enabled")) {
        database.cacheManager.createCache("rpk-travel-bukkit.rpkit_warp.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKWarp::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_warp.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_WARP)
                .column(RPKIT_WARP.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_WARP.NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_WARP.WORLD, SQLDataType.VARCHAR(256))
                .column(RPKIT_WARP.X, SQLDataType.DOUBLE)
                .column(RPKIT_WARP.Y, SQLDataType.DOUBLE)
                .column(RPKIT_WARP.Z, SQLDataType.DOUBLE)
                .column(RPKIT_WARP.YAW, SQLDataType.DOUBLE)
                .column(RPKIT_WARP.PITCH, SQLDataType.DOUBLE)
                .constraints(
                        constraint("pk_rpkit_warp").primaryKey(RPKIT_WARP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKWarp): Int {
        database.create
                .insertInto(
                        RPKIT_WARP,
                        RPKIT_WARP.NAME,
                        RPKIT_WARP.WORLD,
                        RPKIT_WARP.X,
                        RPKIT_WARP.Y,
                        RPKIT_WARP.Z,
                        RPKIT_WARP.YAW,
                        RPKIT_WARP.PITCH
                )
                .values(
                        entity.name,
                        entity.location.world.name,
                        entity.location.x,
                        entity.location.y,
                        entity.location.z,
                        entity.location.yaw.toDouble(),
                        entity.location.pitch.toDouble()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKWarp) {
        database.create
                .update(RPKIT_WARP)
                .set(RPKIT_WARP.NAME, entity.name)
                .set(RPKIT_WARP.WORLD, entity.location.world.name)
                .set(RPKIT_WARP.X, entity.location.x)
                .set(RPKIT_WARP.Y, entity.location.y)
                .set(RPKIT_WARP.Z, entity.location.z)
                .set(RPKIT_WARP.YAW, entity.location.yaw.toDouble())
                .set(RPKIT_WARP.PITCH, entity.location.pitch.toDouble())
                .where(RPKIT_WARP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKWarp? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_WARP.NAME,
                            RPKIT_WARP.WORLD,
                            RPKIT_WARP.X,
                            RPKIT_WARP.Y,
                            RPKIT_WARP.Z,
                            RPKIT_WARP.YAW,
                            RPKIT_WARP.PITCH
                    )
                    .from(RPKIT_WARP)
                    .where(RPKIT_WARP.ID.eq(id))
                    .fetchOne() ?: return null
            val warp = RPKWarpImpl(
                    id,
                    result.get(RPKIT_WARP.NAME),
                    Location(
                            plugin.server.getWorld(result.get(RPKIT_WARP.WORLD)),
                            result.get(RPKIT_WARP.X),
                            result.get(RPKIT_WARP.Y),
                            result.get(RPKIT_WARP.Z),
                            result.get(RPKIT_WARP.YAW).toFloat(),
                            result.get(RPKIT_WARP.PITCH).toFloat()
                    )
            )
            cache?.put(id, warp)
            return warp
        }
    }

    fun get(name: String): RPKWarp? {
        val result = database.create
                .select(RPKIT_WARP.ID)
                .from(RPKIT_WARP)
                .where(RPKIT_WARP.NAME.eq(name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_WARP.ID))
    }

    fun getAll(): List<RPKWarp> {
        val results = database.create
                .select(RPKIT_WARP.ID)
                .from(RPKIT_WARP)
                .fetch()
        return results.map { result -> get(result.get(RPKIT_WARP.ID)) }
                .filterNotNull()
    }

    override fun delete(entity: RPKWarp) {
        database.create
                .deleteFrom(RPKIT_WARP)
                .where(RPKIT_WARP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}