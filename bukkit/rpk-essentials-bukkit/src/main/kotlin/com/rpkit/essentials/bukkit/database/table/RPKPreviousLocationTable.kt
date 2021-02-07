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

package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.create
import com.rpkit.essentials.bukkit.database.jooq.Tables.RPKIT_PREVIOUS_LOCATION
import com.rpkit.essentials.bukkit.locationhistory.RPKPreviousLocation
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.Location


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

    fun insert(entity: RPKPreviousLocation) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
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
                    entity.location.world?.name,
                    entity.location.x,
                    entity.location.y,
                    entity.location.z,
                    entity.location.yaw.toDouble(),
                    entity.location.pitch.toDouble()
                )
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    fun update(entity: RPKPreviousLocation) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .update(RPKIT_PREVIOUS_LOCATION)
                .set(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID, minecraftProfileId.value)
                .set(RPKIT_PREVIOUS_LOCATION.WORLD, entity.location.world?.name)
                .set(RPKIT_PREVIOUS_LOCATION.X, entity.location.x)
                .set(RPKIT_PREVIOUS_LOCATION.Y, entity.location.y)
                .set(RPKIT_PREVIOUS_LOCATION.Z, entity.location.z)
                .set(RPKIT_PREVIOUS_LOCATION.YAW, entity.location.yaw.toDouble())
                .set(RPKIT_PREVIOUS_LOCATION.PITCH, entity.location.pitch.toDouble())
                .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): RPKPreviousLocation? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return cache[minecraftProfileId.value]
        }
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
                .fetchOne() ?: return null
        val previousLocation = RPKPreviousLocation(
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
        cache?.set(minecraftProfileId.value, previousLocation)
        return previousLocation
    }

    fun delete(entity: RPKPreviousLocation) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .deleteFrom(RPKIT_PREVIOUS_LOCATION)
                .where(RPKIT_PREVIOUS_LOCATION.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.remove(minecraftProfileId.value)
    }

}