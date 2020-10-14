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

package com.rpkit.chat.bukkit.database.table

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.chatgroup.RPKLastUsedChatGroup
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_LAST_USED_CHAT_GROUP
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the last used chat group table
 */
class RPKLastUsedChatGroupTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val minecraftProfileCache = if (plugin.config.getBoolean("caching.rpkit_last_used_chat_group.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_last_used_chat_group.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKLastUsedChatGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_last_used_chat_group.minecraft_profile_id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKLastUsedChatGroup) {
        database.create
                .insertInto(
                        RPKIT_LAST_USED_CHAT_GROUP,
                        RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID,
                        RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID
                )
                .values(
                        entity.minecraftProfile.id,
                        entity.chatGroup.id
                )
                .execute()
        minecraftProfileCache?.put(entity.minecraftProfile.id, entity)
    }

    fun update(entity: RPKLastUsedChatGroup) {
        database.create
                .update(RPKIT_LAST_USED_CHAT_GROUP)
                .set(RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID, entity.chatGroup.id)
                .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        minecraftProfileCache?.put(entity.minecraftProfile.id, entity)
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKLastUsedChatGroup? {
        if (minecraftProfileCache?.containsKey(minecraftProfile.id) == true) {
            return minecraftProfileCache.get(minecraftProfile.id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID,
                            RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID
                    )
                    .from(RPKIT_LAST_USED_CHAT_GROUP)
                    .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetchOne() ?: return null
            val chatGroupService = Services[RPKChatGroupService::class] ?: return null
            val chatGroupId = result.get(RPKIT_LAST_USED_CHAT_GROUP.CHAT_GROUP_ID)
            val chatGroup = chatGroupService.getChatGroup(chatGroupId)
            if (chatGroup != null) {
                val lastUsedChatGroup = RPKLastUsedChatGroup(
                        minecraftProfile,
                        chatGroup
                )
                minecraftProfileCache?.put(lastUsedChatGroup.minecraftProfile.id, lastUsedChatGroup)
                return lastUsedChatGroup
            } else {
                database.create
                        .deleteFrom(RPKIT_LAST_USED_CHAT_GROUP)
                        .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                        .execute()
                return null
            }
        }
    }

    fun delete(entity: RPKLastUsedChatGroup) {
        database.create
                .deleteFrom(RPKIT_LAST_USED_CHAT_GROUP)
                .where(RPKIT_LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        minecraftProfileCache?.remove(entity.minecraftProfile.id)
    }

}