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

package com.rpkit.blocklog.bukkit.database.table

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistory
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryId
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChange
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChangeId
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChangeImpl
import com.rpkit.blocklog.bukkit.database.create
import com.rpkit.blocklog.bukkit.database.jooq.Tables.RPKIT_BLOCK_INVENTORY_CHANGE
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.bukkit.util.toItemStackArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService


class RPKBlockInventoryChangeTable(private val database: Database, private val plugin: RPKBlockLoggingBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_block_inventory_change.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-block-logging-bukkit.rpkit_block_inventory_change.id",
            Int::class.javaObjectType,
            RPKBlockInventoryChange::class.java,
            plugin.config.getLong("caching.rpkit_block_inventory_change.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKBlockInventoryChange) {
        database.create
                .insertInto(
                        RPKIT_BLOCK_INVENTORY_CHANGE,
                        RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.TIME,
                        RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.FROM,
                        RPKIT_BLOCK_INVENTORY_CHANGE.TO,
                        RPKIT_BLOCK_INVENTORY_CHANGE.REASON
                )
                .values(
                        entity.blockHistory.id?.value,
                        entity.time,
                        entity.profile?.id?.value,
                        entity.minecraftProfile?.id?.value,
                        entity.character?.id?.value,
                        entity.from.toByteArray(),
                        entity.to.toByteArray(),
                        entity.reason
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKBlockInventoryChangeId(id)
        cache?.set(id, entity)
    }

    fun update(entity: RPKBlockInventoryChange) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_BLOCK_INVENTORY_CHANGE)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID, entity.blockHistory.id?.value)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.TIME, entity.time)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID, entity.profile?.id?.value)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID, entity.minecraftProfile?.id?.value)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID, entity.character?.id?.value)
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.FROM, entity.from.toByteArray())
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.TO, entity.to.toByteArray())
                .set(RPKIT_BLOCK_INVENTORY_CHANGE.REASON, entity.reason)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
    }

    operator fun get(id: RPKBlockInventoryChangeId): RPKBlockInventoryChange? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        }
        val result = database.create
                .select(
                        RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.TIME,
                        RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID,
                        RPKIT_BLOCK_INVENTORY_CHANGE.FROM,
                        RPKIT_BLOCK_INVENTORY_CHANGE.TO,
                        RPKIT_BLOCK_INVENTORY_CHANGE.REASON
                )
                .from(RPKIT_BLOCK_INVENTORY_CHANGE)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(id.value))
                .fetchOne() ?: return null
        val blockHistoryService = Services[RPKBlockHistoryService::class.java] ?: return null
        val blockHistoryId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID)
        val blockHistory = blockHistoryService.getBlockHistory(RPKBlockHistoryId(blockHistoryId))
        if (blockHistory == null) {
            database.create
                    .deleteFrom(RPKIT_BLOCK_INVENTORY_CHANGE)
                    .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(id.value))
                    .execute()
            cache?.remove(id.value)
            return null
        }
        val profileService = Services[RPKProfileService::class.java] ?: return null
        val profileId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.PROFILE_ID)
        val profile = if (profileId == null) null else profileService.getProfile(RPKProfileId(profileId))
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return null
        val minecraftProfileId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.MINECRAFT_PROFILE_ID)
        val minecraftProfile = if (minecraftProfileId == null) {
            null
        } else {
            minecraftProfileService.getMinecraftProfile(RPKMinecraftProfileId(minecraftProfileId))
        }
        val characterService = Services[RPKCharacterService::class.java] ?: return null
        val characterId = result.get(RPKIT_BLOCK_INVENTORY_CHANGE.CHARACTER_ID)
        val character = if (characterId == null) null else characterService.getCharacter(RPKCharacterId(characterId))
        val blockInventoryChange = RPKBlockInventoryChangeImpl(
                id,
                blockHistory,
                result.get(RPKIT_BLOCK_INVENTORY_CHANGE.TIME),
                profile,
                minecraftProfile,
                character,
                result.get(RPKIT_BLOCK_INVENTORY_CHANGE.FROM).toItemStackArray(),
                result.get(RPKIT_BLOCK_INVENTORY_CHANGE.TO).toItemStackArray(),
                result.get(RPKIT_BLOCK_INVENTORY_CHANGE.REASON)
        )
        cache?.set(id.value, blockInventoryChange)
        return blockInventoryChange
    }

    fun get(blockHistory: RPKBlockHistory): List<RPKBlockInventoryChange> {
        val results = database.create
                .select(RPKIT_BLOCK_INVENTORY_CHANGE.ID)
                .from(RPKIT_BLOCK_INVENTORY_CHANGE)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.BLOCK_HISTORY_ID.eq(blockHistory.id?.value))
                .fetch()
        return results
                .map { result -> get(RPKBlockInventoryChangeId(result[RPKIT_BLOCK_INVENTORY_CHANGE.ID])) }
                .filterNotNull()
    }

    fun delete(entity: RPKBlockInventoryChange) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_BLOCK_INVENTORY_CHANGE)
                .where(RPKIT_BLOCK_INVENTORY_CHANGE.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }
}