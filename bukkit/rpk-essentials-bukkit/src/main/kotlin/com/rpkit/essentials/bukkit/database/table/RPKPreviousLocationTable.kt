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

package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKLocation
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.create
import com.rpkit.essentials.bukkit.database.jooq.Tables.RPKIT_PREVIOUS_LOCATION
import com.rpkit.essentials.bukkit.locationhistory.RPKPreviousLocation
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level
import java.util.logging.Level.SEVERE


class RPKPreviousLocationTable(private val database: Database, private val plugin: RPKEssentialsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_previous_location.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-essentials-bukkit.rpkit_previous_location.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKPreviousLocation::class.java,
            plugin.config.getLong("caching.rpkit_previous_location.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPreviousLocation): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
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
                    minecraftProfileId.value,
                    entity.location.world,
                    entity.location.x,
                    entity.location.y,
                    entity.location.z,
                    entity.location.yaw.toDouble(),
                    entity.location.pitch.toDouble()
                )
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert previous location", exception)
            throw exception
        }
    }

    fun update(entity: RPKPreviousLocation): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_PREVIOUS_LOCATION)
                .set(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID, minecraftProfileId.value)
                .set(RPKIT_PREVIOUS_LOCATION.WORLD, entity.location.world)
                .set(RPKIT_PREVIOUS_LOCATION.X, entity.location.x)
                .set(RPKIT_PREVIOUS_LOCATION.Y, entity.location.y)
                .set(RPKIT_PREVIOUS_LOCATION.Z, entity.location.z)
                .set(RPKIT_PREVIOUS_LOCATION.YAW, entity.location.yaw.toDouble())
                .set(RPKIT_PREVIOUS_LOCATION.PITCH, entity.location.pitch.toDouble())
                .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update previous location", exception)
            throw exception
        }
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKPreviousLocation?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(cache[minecraftProfileId.value])
        }
        return CompletableFuture.supplyAsync {
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
                .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetchOne() ?: return@supplyAsync null
            val previousLocation = RPKPreviousLocation(
                minecraftProfile,
                RPKLocation(
                    result.get(RPKIT_PREVIOUS_LOCATION.WORLD),
                    result.get(RPKIT_PREVIOUS_LOCATION.X),
                    result.get(RPKIT_PREVIOUS_LOCATION.Y),
                    result.get(RPKIT_PREVIOUS_LOCATION.Z),
                    result.get(RPKIT_PREVIOUS_LOCATION.YAW).toFloat(),
                    result.get(RPKIT_PREVIOUS_LOCATION.PITCH).toFloat()
                )
            )
            cache?.set(minecraftProfileId.value, previousLocation)
            return@supplyAsync previousLocation
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get previous location", exception)
            throw exception
        }
    }

    fun delete(entity: RPKPreviousLocation): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_PREVIOUS_LOCATION)
                .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.remove(minecraftProfileId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete previous location", exception)
            throw exception
        }
    }

    fun delete(minecraftProfileId: RPKMinecraftProfileId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_PREVIOUS_LOCATION)
            .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .execute()
        cache?.remove(minecraftProfileId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete previous location for Minecraft profile id", exception)
        throw exception
    }

}