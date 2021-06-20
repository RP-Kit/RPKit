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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupId
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.chatgroup.RPKLastUsedChatGroup
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_LAST_USED_CHAT_GROUP
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture

/**
 * Represents the last used chat group table
 */
class RPKLastUsedChatGroupTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val minecraftProfileCache = if (plugin.config.getBoolean("caching.rpkit_last_used_chat_group.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-chat-bukkit.rpkit_last_used_chat_group.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKLastUsedChatGroup::class.java,
            plugin.config.getLong("caching.rpkit_last_used_chat_group.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKLastUsedChatGroup): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        val chatGroupId = entity.chatGroup.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_LAST_USED_CHAT_GROUP,
                    RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID,
                    RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID
                )
                .values(
                    minecraftProfileId.value,
                    chatGroupId.value
                )
                .execute()
            minecraftProfileCache?.set(minecraftProfileId.value, entity)
        }
    }

    fun update(entity: RPKLastUsedChatGroup): CompletableFuture<Void> {
        val chatGroupId = entity.chatGroup.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_LAST_USED_CHAT_GROUP)
                .set(RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID, chatGroupId.value)
                .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            minecraftProfileCache?.set(minecraftProfileId.value, entity)
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKLastUsedChatGroup?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (minecraftProfileCache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(minecraftProfileCache[minecraftProfileId.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID,
                    RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID
                )
                .from(RPKIT_LAST_USED_CHAT_GROUP)
                .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetchOne() ?: return@supplyAsync null
            val chatGroupService = Services[RPKChatGroupService::class.java] ?: return@supplyAsync null
            val chatGroupId = result.get(RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID)
            val chatGroup = chatGroupService.getChatGroup(RPKChatGroupId(chatGroupId)).join()
            if (chatGroup != null) {
                val lastUsedChatGroup = RPKLastUsedChatGroup(
                    minecraftProfile,
                    chatGroup
                )
                minecraftProfileCache?.set(minecraftProfileId.value, lastUsedChatGroup)
                return@supplyAsync lastUsedChatGroup
            } else {
                database.create
                    .deleteFrom(RPKIT_LAST_USED_CHAT_GROUP)
                    .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .execute()
                return@supplyAsync null
            }
        }
    }

    fun delete(entity: RPKLastUsedChatGroup): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_LAST_USED_CHAT_GROUP)
                .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            minecraftProfileCache?.remove(minecraftProfileId.value)
        }
    }

}