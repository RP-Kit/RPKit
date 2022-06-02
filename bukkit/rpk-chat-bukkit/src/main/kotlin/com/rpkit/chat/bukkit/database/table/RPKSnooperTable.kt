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
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Represents the snooper table.
 */
class RPKSnooperTable(
        private val database: Database,
        private val plugin: RPKChatBukkit
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

    fun insert(entity: RPKSnooper): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_SNOOPER,
                    RPKIT_SNOOPER.MINECRAFT_PROFILE_ID
                )
                .values(minecraftProfileId.value)
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert snooper", exception)
            throw exception
        }
    }

    fun update(entity: RPKSnooper): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_SNOOPER)
                .set(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID, minecraftProfileId.value)
                .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update snooper", exception)
            throw exception
        }
    }

    /**
     * Gets the snooper instance for a Minecraft profile.
     * If the player does not have a snooper entry, null is returned.
     *
     * @param minecraftProfile The player
     * @return The snooper instance, or null if none exists
     */
    fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKSnooper?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(cache[minecraftProfileId.value])
        } else {
            return CompletableFuture.supplyAsync {
                database.create
                    .select(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_SNOOPER)
                    .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .fetchOne() ?: return@supplyAsync null
                val snooper = RPKSnooper(
                    minecraftProfile
                )
                cache?.set(minecraftProfileId.value, snooper)
                return@supplyAsync snooper
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get snooper", exception)
                throw exception
            }
        }
    }

    /**
     * Gets all snoopers
     *
     * @return A list containing all snoopers
     */
    fun getAll(): CompletableFuture<List<RPKSnooper>> {
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)
                .from(RPKIT_SNOOPER)
                .fetch()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return@supplyAsync emptyList()
            return@supplyAsync results.mapNotNull { result ->
                val minecraftProfile =
                    minecraftProfileService.getMinecraftProfile(RPKMinecraftProfileId(result.get(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)))
                        .join()
                        ?: return@mapNotNull null
                return@mapNotNull RPKSnooper(minecraftProfile)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get all snoopers", exception)
            throw exception
        }
    }

    fun delete(entity: RPKSnooper): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_SNOOPER)
                .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.remove(minecraftProfileId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete snooper", exception)
            throw exception
        }
    }

}