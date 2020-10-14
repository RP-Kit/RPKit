/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.blocklog.bukkit.database.table

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChange
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistory
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.blocklog.bukkit.database.jooq.Tables.RPKIT_BLOCK_CHANGE
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.bukkit.Material
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class RPKBlockChangeTable(private val database: Database, private val plugin: RPKBlockLoggingBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_block_change.id.enabled")) {
        database.cacheManager.createCache("rpk-block-logging-bukkit.rpkit_block_change.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBlockChange::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_block_change.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKBlockChange) {
        database.create
                .insertInto(
                        RPKIT_BLOCK_CHANGE,
                        RPKIT_BLOCK_CHANGE.BLOCK_HISTORY_ID,
                        RPKIT_BLOCK_CHANGE.TIME,
                        RPKIT_BLOCK_CHANGE.PROFILE_ID,
                        RPKIT_BLOCK_CHANGE.MINECRAFT_PROFILE_ID,
                        RPKIT_BLOCK_CHANGE.CHARACTER_ID,
                        RPKIT_BLOCK_CHANGE.FROM,
                        RPKIT_BLOCK_CHANGE.TO,
                        RPKIT_BLOCK_CHANGE.REASON
                )
                .values(
                        entity.blockHistory.id,
                        entity.time,
                        entity.profile?.id,
                        entity.minecraftProfile?.id,
                        entity.character?.id,
                        entity.from.toString(),
                        entity.to.toString(),
                        entity.reason
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKBlockChange) {
        database.create
                .update(RPKIT_BLOCK_CHANGE)
                .set(RPKIT_BLOCK_CHANGE.BLOCK_HISTORY_ID, entity.blockHistory.id)
                .set(RPKIT_BLOCK_CHANGE.TIME, entity.time)
                .set(RPKIT_BLOCK_CHANGE.PROFILE_ID, entity.profile?.id)
                .set(RPKIT_BLOCK_CHANGE.MINECRAFT_PROFILE_ID, entity.minecraftProfile?.id)
                .set(RPKIT_BLOCK_CHANGE.CHARACTER_ID, entity.character?.id)
                .set(RPKIT_BLOCK_CHANGE.FROM, entity.from.toString())
                .set(RPKIT_BLOCK_CHANGE.TO, entity.to.toString())
                .set(RPKIT_BLOCK_CHANGE.REASON, entity.reason)
                .where(RPKIT_BLOCK_CHANGE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKBlockChange? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_BLOCK_CHANGE.BLOCK_HISTORY_ID,
                            RPKIT_BLOCK_CHANGE.TIME,
                            RPKIT_BLOCK_CHANGE.PROFILE_ID,
                            RPKIT_BLOCK_CHANGE.MINECRAFT_PROFILE_ID,
                            RPKIT_BLOCK_CHANGE.CHARACTER_ID,
                            RPKIT_BLOCK_CHANGE.FROM,
                            RPKIT_BLOCK_CHANGE.TO,
                            RPKIT_BLOCK_CHANGE.REASON
                    )
                    .from(RPKIT_BLOCK_CHANGE)
                    .where(RPKIT_BLOCK_CHANGE.ID.eq(id))
                    .fetchOne() ?: return null
            val blockHistoryService = Services[RPKBlockHistoryService::class] ?: return null
            val blockHistoryId = result.get(RPKIT_BLOCK_CHANGE.BLOCK_HISTORY_ID)
            val blockHistory = blockHistoryService.getBlockHistory(blockHistoryId)
            if (blockHistory == null) {
                database.create
                        .deleteFrom(RPKIT_BLOCK_CHANGE)
                        .where(RPKIT_BLOCK_CHANGE.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
            val profileService = Services[RPKProfileService::class] ?: return null
            val profileId = result.get(RPKIT_BLOCK_CHANGE.PROFILE_ID)
            val profile = if (profileId == null) null else profileService.getProfile(profileId)
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return null
            val minecraftProfileId = result.get(RPKIT_BLOCK_CHANGE.MINECRAFT_PROFILE_ID)
            val minecraftProfile = if (minecraftProfileId == null) null else minecraftProfileService.getMinecraftProfile(minecraftProfileId)
            val characterService = Services[RPKCharacterService::class] ?: return null
            val characterId = result.get(RPKIT_BLOCK_CHANGE.CHARACTER_ID)
            val character = if (characterId == null) null else characterService.getCharacter(characterId)
            val fromMaterial = Material.getMaterial(result.get(RPKIT_BLOCK_CHANGE.FROM))
                    ?: Material.getMaterial(result.get(RPKIT_BLOCK_CHANGE.FROM), true)
            val toMaterial = Material.getMaterial(result.get(RPKIT_BLOCK_CHANGE.TO))
                    ?: Material.getMaterial(result.get(RPKIT_BLOCK_CHANGE.TO), true)
            if (fromMaterial == null || toMaterial == null) {
                database.create
                        .deleteFrom(RPKIT_BLOCK_CHANGE)
                        .where(RPKIT_BLOCK_CHANGE.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
            val blockChange = RPKBlockChangeImpl(
                    id,
                    blockHistory,
                    result.get(RPKIT_BLOCK_CHANGE.TIME),
                    profile,
                    minecraftProfile,
                    character,
                    fromMaterial,
                    toMaterial,
                    result.get(RPKIT_BLOCK_CHANGE.REASON)
            )
            cache?.put(id, blockChange)
            return blockChange
        }
    }

    fun get(blockHistory: RPKBlockHistory): List<RPKBlockChange> {
        val results = database.create
                .select(RPKIT_BLOCK_CHANGE.ID)
                .from(RPKIT_BLOCK_CHANGE)
                .where(RPKIT_BLOCK_CHANGE.BLOCK_HISTORY_ID.eq(blockHistory.id))
                .fetch()
        return results
                .map { result -> get(result[RPKIT_BLOCK_CHANGE.ID]) }
                .filterNotNull()
    }

    fun delete(entity: RPKBlockChange) {
        database.create
                .deleteFrom(RPKIT_BLOCK_CHANGE)
                .where(RPKIT_BLOCK_CHANGE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}