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

package com.rpkit.chat.bukkit.database.table

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_SNOOPER
import com.rpkit.chat.bukkit.snooper.RPKSnooper
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService

/**
 * Represents the snooper table.
 */
class RPKSnooperTable(
        private val database: Database,
        plugin: RPKChatBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_snooper.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-chat-bukkit.rpkit_snooper.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKSnooper::class.java,
            plugin.config.getLong("caching.rpkit_snooper.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKSnooper) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .insertInto(
                        RPKIT_SNOOPER,
                        RPKIT_SNOOPER.MINECRAFT_PROFILE_ID
                )
                .values(minecraftProfileId.value)
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    fun update(entity: RPKSnooper) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .update(RPKIT_SNOOPER)
                .set(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID, minecraftProfileId.value)
                .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    /**
     * Gets the snooper instance for a Minecraft profile.
     * If the player does not have a snooper entry, null is returned.
     *
     * @param minecraftProfile The player
     * @return The snooper instance, or null if none exists
     */
    fun get(minecraftProfile: RPKMinecraftProfile): RPKSnooper? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return cache.get(minecraftProfileId.value)
        } else {
            database.create
                    .select(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_SNOOPER)
                    .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .fetchOne() ?: return null
            val snooper = RPKSnooper(
                    minecraftProfile
            )
            cache?.set(minecraftProfileId.value, snooper)
            return snooper
        }
    }

    /**
     * Gets all snoopers
     *
     * @return A list containing all snoopers
     */
    fun getAll(): List<RPKSnooper> {
        val results = database.create
                .select(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)
                .from(RPKIT_SNOOPER)
                .fetch()
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return emptyList()
        return results.mapNotNull { result ->
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftProfileId(result.get(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)))
                    ?: return@mapNotNull null
            return@mapNotNull RPKSnooper(minecraftProfile)
        }
    }

    fun delete(entity: RPKSnooper) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .deleteFrom(RPKIT_SNOOPER)
                .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.remove(minecraftProfileId.value)
    }

}