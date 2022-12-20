/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.travel.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKLocation
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.create
import com.rpkit.travel.bukkit.database.jooq.Tables.RPKIT_WARP
import com.rpkit.travel.bukkit.warp.RPKWarpImpl
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpName
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


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

    fun insert(entity: RPKWarp): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
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
                    entity.location.world,
                    entity.location.x,
                    entity.location.y,
                    entity.location.z,
                    entity.location.yaw,
                    entity.location.pitch
                )
                .execute()
            cache?.set(entity.name.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert warp", exception)
            throw exception
        }
    }

    fun update(entity: RPKWarp): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_WARP)
                .set(RPKIT_WARP.WORLD, entity.location.world)
                .set(RPKIT_WARP.X, entity.location.x)
                .set(RPKIT_WARP.Y, entity.location.y)
                .set(RPKIT_WARP.Z, entity.location.z)
                .set(RPKIT_WARP.YAW, entity.location.yaw)
                .set(RPKIT_WARP.PITCH, entity.location.pitch)
                .where(RPKIT_WARP.NAME.eq(entity.name.value))
                .execute()
            cache?.set(entity.name.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update warp", exception)
            throw exception
        }
    }

    operator fun get(name: String): CompletableFuture<out RPKWarp?> {
        if (cache?.containsKey(name) == true) {
            return CompletableFuture.completedFuture(cache[name])
        } else {
            return CompletableFuture.supplyAsync {
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
                    .fetchOne() ?: return@supplyAsync null
                val warp = RPKWarpImpl(
                    RPKWarpName(result.get(RPKIT_WARP.NAME)),
                    RPKLocation(
                        result.get(RPKIT_WARP.WORLD),
                        result.get(RPKIT_WARP.X),
                        result.get(RPKIT_WARP.Y),
                        result.get(RPKIT_WARP.Z),
                        result.get(RPKIT_WARP.YAW).toFloat(),
                        result.get(RPKIT_WARP.PITCH).toFloat()
                    )
                )
                cache?.set(name, warp)
                return@supplyAsync warp
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get warp", exception)
                throw exception
            }
        }
    }

    fun getAll(): CompletableFuture<out List<RPKWarp>> {
        return CompletableFuture.supplyAsync {
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
            return@supplyAsync results.mapNotNull { result ->
                RPKWarpImpl(
                    RPKWarpName(result.get(RPKIT_WARP.NAME)),
                    RPKLocation(
                        result.get(RPKIT_WARP.WORLD),
                        result.get(RPKIT_WARP.X),
                        result.get(RPKIT_WARP.Y),
                        result.get(RPKIT_WARP.Z),
                        result.get(RPKIT_WARP.YAW).toFloat(),
                        result.get(RPKIT_WARP.PITCH).toFloat()
                    )
                )
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get all warps", exception)
            throw exception
        }
    }

    fun delete(entity: RPKWarp): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_WARP)
                .where(RPKIT_WARP.NAME.eq(entity.name.value))
                .execute()
            cache?.remove(entity.name.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete warp", exception)
            throw exception
        }
    }

}
