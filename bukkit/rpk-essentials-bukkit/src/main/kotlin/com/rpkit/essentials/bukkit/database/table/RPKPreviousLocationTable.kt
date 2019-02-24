package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.jooq.rpkit.Tables.RPKIT_PREVIOUS_LOCATION
import com.rpkit.essentials.bukkit.locationhistory.RPKPreviousLocation
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType


class RPKPreviousLocationTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKPreviousLocation>(database, RPKPreviousLocation::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_previous_location.id.enabled")) {
        database.cacheManager.createCache("rpk-essentials-bukkit.rpkit_previous_location.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPreviousLocation::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_previous_location.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PREVIOUS_LOCATION)
                .column(RPKIT_PREVIOUS_LOCATION.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_PREVIOUS_LOCATION.WORLD, SQLDataType.VARCHAR(256))
                .column(RPKIT_PREVIOUS_LOCATION.X, SQLDataType.DOUBLE)
                .column(RPKIT_PREVIOUS_LOCATION.Y, SQLDataType.DOUBLE)
                .column(RPKIT_PREVIOUS_LOCATION.Z, SQLDataType.DOUBLE)
                .column(RPKIT_PREVIOUS_LOCATION.YAW, SQLDataType.DOUBLE)
                .column(RPKIT_PREVIOUS_LOCATION.PITCH, SQLDataType.DOUBLE)
                .constraints(
                        constraint("pk_rpkit_previous_location").primaryKey(RPKIT_PREVIOUS_LOCATION.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.create
                    .truncate(RPKIT_PREVIOUS_LOCATION)
                    .execute()
            database.create
                    .alterTable(RPKIT_PREVIOUS_LOCATION)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_PREVIOUS_LOCATION)
                    .addColumn(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKPreviousLocation): Int {
        database.create
                .insertInto(
                        RPKIT_PREVIOUS_LOCATION,
                        RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID,
                        RPKIT_PREVIOUS_LOCATION.WORLD,
                        RPKIT_PREVIOUS_LOCATION.X,
                        RPKIT_PREVIOUS_LOCATION.Y,
                        RPKIT_PREVIOUS_LOCATION.Z,
                        RPKIT_PREVIOUS_LOCATION.YAW,
                        RPKIT_PREVIOUS_LOCATION.PITCH
                )
                .values(
                        entity.minecraftProfile.id,
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

    override fun update(entity: RPKPreviousLocation) {
        database.create
                .update(RPKIT_PREVIOUS_LOCATION)
                .set(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(RPKIT_PREVIOUS_LOCATION.WORLD, entity.location.world.name)
                .set(RPKIT_PREVIOUS_LOCATION.X, entity.location.x)
                .set(RPKIT_PREVIOUS_LOCATION.Y, entity.location.y)
                .set(RPKIT_PREVIOUS_LOCATION.Z, entity.location.z)
                .set(RPKIT_PREVIOUS_LOCATION.YAW, entity.location.yaw.toDouble())
                .set(RPKIT_PREVIOUS_LOCATION.PITCH, entity.location.pitch.toDouble())
                .where(RPKIT_PREVIOUS_LOCATION.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKPreviousLocation? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID,
                            RPKIT_PREVIOUS_LOCATION.WORLD,
                            RPKIT_PREVIOUS_LOCATION.X,
                            RPKIT_PREVIOUS_LOCATION.Y,
                            RPKIT_PREVIOUS_LOCATION.Z,
                            RPKIT_PREVIOUS_LOCATION.YAW,
                            RPKIT_PREVIOUS_LOCATION.PITCH
                    )
                    .from(RPKIT_PREVIOUS_LOCATION)
                    .where(RPKIT_PREVIOUS_LOCATION.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            if (minecraftProfile != null) {
                val previousLocation = RPKPreviousLocation(
                        id,
                        minecraftProfile,
                        Location(
                                plugin.server.getWorld(result.get(RPKIT_PREVIOUS_LOCATION.WORLD)),
                                result.get(RPKIT_PREVIOUS_LOCATION.X),
                                result.get(RPKIT_PREVIOUS_LOCATION.Y),
                                result.get(RPKIT_PREVIOUS_LOCATION.Z),
                                result.get(RPKIT_PREVIOUS_LOCATION.YAW).toFloat(),
                                result.get(RPKIT_PREVIOUS_LOCATION.PITCH).toFloat()
                        )
                )
                cache?.put(id, previousLocation)
                return previousLocation
            } else {
                database.create
                        .deleteFrom(RPKIT_PREVIOUS_LOCATION)
                        .where(RPKIT_PREVIOUS_LOCATION.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKPreviousLocation? {
        val result = database.create
                .select(RPKIT_PREVIOUS_LOCATION.ID)
                .from(RPKIT_PREVIOUS_LOCATION)
                .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_PREVIOUS_LOCATION.ID])
    }

    override fun delete(entity: RPKPreviousLocation) {
        database.create
                .deleteFrom(RPKIT_PREVIOUS_LOCATION)
                .where(RPKIT_PREVIOUS_LOCATION.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}