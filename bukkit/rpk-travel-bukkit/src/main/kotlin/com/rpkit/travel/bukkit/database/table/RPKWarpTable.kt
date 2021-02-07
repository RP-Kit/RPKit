/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.travel.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.create
import com.rpkit.travel.bukkit.database.jooq.Tables.RPKIT_WARP
import com.rpkit.travel.bukkit.warp.RPKWarpImpl
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpName
import org.bukkit.Location


class RPKWarpTable(private val database: Database, private val plugin: RPKTravelBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_warp.name.enabled")) {
        database.cacheManager.createCache(
            "rpk-travel-bukkit.rpkit_warp.name",
            String::class.java,
            RPKWarp::class.java,
            plugin.config.getLong("caching.rpkit_warp.name.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKWarp) {
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
                        entity.name.value,
                        entity.location.world?.name,
                        entity.location.x,
                        entity.location.y,
                        entity.location.z,
                        entity.location.yaw.toDouble(),
                        entity.location.pitch.toDouble()
                )
                .execute()
        cache?.set(entity.name.value, entity)
    }

    fun update(entity: RPKWarp) {
        database.create
                .update(RPKIT_WARP)
                .set(RPKIT_WARP.WORLD, entity.location.world?.name)
                .set(RPKIT_WARP.X, entity.location.x)
                .set(RPKIT_WARP.Y, entity.location.y)
                .set(RPKIT_WARP.Z, entity.location.z)
                .set(RPKIT_WARP.YAW, entity.location.yaw.toDouble())
                .set(RPKIT_WARP.PITCH, entity.location.pitch.toDouble())
                .where(RPKIT_WARP.NAME.eq(entity.name.value))
                .execute()
        cache?.set(entity.name.value, entity)
    }

    operator fun get(name: String): RPKWarp? {
        if (cache?.containsKey(name) == true) {
            return cache[name]
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
                    .where(RPKIT_WARP.NAME.eq(name))
                    .fetchOne() ?: return null
            val warp = RPKWarpImpl(
                    RPKWarpName(result.get(RPKIT_WARP.NAME)),
                    Location(
                            plugin.server.getWorld(result.get(RPKIT_WARP.WORLD)),
                            result.get(RPKIT_WARP.X),
                            result.get(RPKIT_WARP.Y),
                            result.get(RPKIT_WARP.Z),
                            result.get(RPKIT_WARP.YAW).toFloat(),
                            result.get(RPKIT_WARP.PITCH).toFloat()
                    )
            )
            cache?.set(name, warp)
            return warp
        }
    }

    fun getAll(): List<RPKWarp> {
        val results = database.create
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
                .fetch()
        return results.mapNotNull { result ->
            RPKWarpImpl(
                    RPKWarpName(result.get(RPKIT_WARP.NAME)),
                    Location(
                            plugin.server.getWorld(result.get(RPKIT_WARP.WORLD)),
                            result.get(RPKIT_WARP.X),
                            result.get(RPKIT_WARP.Y),
                            result.get(RPKIT_WARP.Z),
                            result.get(RPKIT_WARP.YAW).toFloat(),
                            result.get(RPKIT_WARP.PITCH).toFloat()
                    )
            )
        }
    }

    fun delete(entity: RPKWarp) {
        database.create
                .deleteFrom(RPKIT_WARP)
                .where(RPKIT_WARP.NAME.eq(entity.name.value))
                .execute()
        cache?.remove(entity.name.value)
    }

}