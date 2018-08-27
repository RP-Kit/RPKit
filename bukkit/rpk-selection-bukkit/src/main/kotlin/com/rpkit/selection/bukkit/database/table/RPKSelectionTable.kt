/*
 * Copyright 2018 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.selection.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.database.jooq.rpkit.Tables.RPKIT_SELECTION
import com.rpkit.selection.bukkit.selection.RPKSelection
import com.rpkit.selection.bukkit.selection.RPKSelectionImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKSelectionTable(database: Database, private val plugin: RPKSelectionBukkit): Table<RPKSelection>(database, RPKSelection::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_selection.id.enabled")) {
        database.cacheManager.createCache("rpk-selection-bukkit.rpkit_selection.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKSelection::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_selection.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_SELECTION)
                .column(RPKIT_SELECTION.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER else SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.WORLD, SQLDataType.VARCHAR(256))
                .column(RPKIT_SELECTION.X_1, SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.Y_1, SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.Z_1, SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.X_2, SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.Y_2, SQLDataType.INTEGER)
                .column(RPKIT_SELECTION.Z_2, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_selection").primaryKey(RPKIT_SELECTION.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.6.0")
        }
    }

    override fun insert(entity: RPKSelection): Int {
        database.create.
                insertInto(
                        RPKIT_SELECTION,
                        RPKIT_SELECTION.MINECRAFT_PROFILE_ID,
                        RPKIT_SELECTION.WORLD,
                        RPKIT_SELECTION.X_1,
                        RPKIT_SELECTION.Y_1,
                        RPKIT_SELECTION.Z_1,
                        RPKIT_SELECTION.X_2,
                        RPKIT_SELECTION.Y_2,
                        RPKIT_SELECTION.Z_2
                )
                .values(
                        entity.minecraftProfile.id,
                        entity.world.name,
                        entity.minimumPoint.x,
                        entity.minimumPoint.y,
                        entity.minimumPoint.z,
                        entity.maximumPoint.x,
                        entity.maximumPoint.y,
                        entity.maximumPoint.z
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKSelection) {
        database.create
                .update(RPKIT_SELECTION)
                .set(RPKIT_SELECTION.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(RPKIT_SELECTION.WORLD, entity.world.name)
                .set(RPKIT_SELECTION.X_1, entity.minimumPoint.x)
                .set(RPKIT_SELECTION.Y_1, entity.minimumPoint.y)
                .set(RPKIT_SELECTION.Z_1, entity.minimumPoint.z)
                .set(RPKIT_SELECTION.X_2, entity.maximumPoint.x)
                .set(RPKIT_SELECTION.Y_2, entity.maximumPoint.y)
                .set(RPKIT_SELECTION.Z_2, entity.maximumPoint.z)
                .where(RPKIT_SELECTION.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKSelection? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create.select(
                            RPKIT_SELECTION.MINECRAFT_PROFILE_ID,
                            RPKIT_SELECTION.WORLD,
                            RPKIT_SELECTION.X_1,
                            RPKIT_SELECTION.Y_1,
                            RPKIT_SELECTION.Z_1,
                            RPKIT_SELECTION.X_2,
                            RPKIT_SELECTION.Y_2,
                            RPKIT_SELECTION.Z_2
                    )
                    .from(RPKIT_SELECTION)
                    .where(RPKIT_SELECTION.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class.java)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(result[RPKIT_SELECTION.MINECRAFT_PROFILE_ID])
            if (minecraftProfile == null) {
                database.create
                        .deleteFrom(RPKIT_SELECTION)
                        .where(RPKIT_SELECTION.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
            val world = plugin.server.getWorld(result[RPKIT_SELECTION.WORLD])
            val block1 = world.getBlockAt(
                    result[RPKIT_SELECTION.X_1],
                    result[RPKIT_SELECTION.Y_1],
                    result[RPKIT_SELECTION.Z_1]
            )
            val block2 = world.getBlockAt(
                    result[RPKIT_SELECTION.X_2],
                    result[RPKIT_SELECTION.Y_2],
                    result[RPKIT_SELECTION.Z_2]
            )
            val selection = RPKSelectionImpl(
                    id,
                    minecraftProfile,
                    world,
                    block1,
                    block2
            )
            cache?.put(id, selection)
            return selection
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKSelection? {
        val result = database.create
                .select(RPKIT_SELECTION.ID)
                .from(RPKIT_SELECTION)
                .where(RPKIT_SELECTION.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_SELECTION.ID])
    }

    override fun delete(entity: RPKSelection) {
        database.create
                .deleteFrom(RPKIT_SELECTION)
                .where(RPKIT_SELECTION.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}