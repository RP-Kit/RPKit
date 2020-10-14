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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupMember
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_GROUP_MEMBER
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the chat group member table.
 */
class RPKChatGroupMemberTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val chatGroupCache = if (plugin.config.getBoolean("caching.rpkit_chat_group_member.chat_group_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_group_member.chat_group_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_chat_group_member.chat_group_id.size"))).build())
    } else {
        null
    }

    private val minecraftProfileCache = if (plugin.config.getBoolean("caching.rpkit_chat_group_member.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_group_member.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_chat_group_member.minecraft_profile_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKChatGroupMember) {
        database.create
                .insertInto(
                        RPKIT_CHAT_GROUP_MEMBER,
                        RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID,
                        RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.chatGroup.id,
                        entity.minecraftProfile.id
                )
                .execute()
        cache(entity)
    }

    /**
     * Gets a list of members of a specific chat group.
     *
     * @param chatGroup The chat group
     * @return A list of members of the chat group
     */
    fun get(chatGroup: RPKChatGroup): List<RPKChatGroupMember> {
        return if (chatGroupCache?.containsKey(chatGroup.id) == true) {
            chatGroupCache.get(chatGroup.id) as List<RPKChatGroupMember>
        } else {
            val results = database.create
                    .select(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_CHAT_GROUP_MEMBER)
                    .where(RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID.eq(chatGroup.id))
                    .fetch()
            val chatGroupMembers = results.mapNotNull { result ->
                val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return@mapNotNull null
                val minecraftProfile = minecraftProfileService
                        .getMinecraftProfile(result[RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID])
                        ?: return@mapNotNull null
                RPKChatGroupMember(
                        chatGroup,
                        minecraftProfile
                )
            }
            chatGroupCache?.put(chatGroup.id, chatGroupMembers.toMutableList())
            chatGroupMembers
        }
    }

    /**
     * Gets a list of chat group member instances for a Minecraft profile
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group member instances
     */
    fun get(minecraftProfile: RPKMinecraftProfile): List<RPKChatGroupMember> {
        return if (minecraftProfileCache?.containsKey(minecraftProfile.id) == true) {
            minecraftProfileCache.get(minecraftProfile.id) as List<RPKChatGroupMember>
        } else {
            val results = database.create
                    .select(RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID)
                    .from(RPKIT_CHAT_GROUP_MEMBER)
                    .where(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetch()
            val chatGroupMembers = results.mapNotNull { result ->
                val chatGroupService = Services[RPKChatGroupService::class] ?: return@mapNotNull null
                val chatGroup = chatGroupService
                        .getChatGroup(result[RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID])
                        ?: return@mapNotNull null
                RPKChatGroupMember(
                        chatGroup,
                        minecraftProfile
                )
            }
            minecraftProfileCache?.put(minecraftProfile.id, chatGroupMembers.toMutableList())
            chatGroupMembers
        }
    }

    fun delete(entity: RPKChatGroupMember) {
        database.create
                .deleteFrom(RPKIT_CHAT_GROUP_MEMBER)
                .where(RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID.eq(entity.chatGroup.id))
                .and(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        uncache(entity)
    }

    private fun cache(chatGroupMember: RPKChatGroupMember) {
        val chatGroupMembers = chatGroupCache?.get(chatGroupMember.chatGroup.id) as? MutableList<RPKChatGroupMember> ?: mutableListOf()
        if (!chatGroupMember.let { chatGroupMembers.contains(it) }) {
            chatGroupMember.let { chatGroupMembers.add(it) }
        }
        chatGroupCache?.put(chatGroupMember.chatGroup.id, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(chatGroupMember.minecraftProfile.id) as? MutableList<RPKChatGroupMember>
                ?: mutableListOf()
        if (!chatGroupMember.let { minecraftProfileMembers.contains(it) }) {
            chatGroupMember.let { minecraftProfileMembers.add(it) }
        }
        minecraftProfileCache?.put(chatGroupMember.minecraftProfile.id, minecraftProfileMembers)
    }

    private fun uncache(chatGroupMember: RPKChatGroupMember) {
        val chatGroupMembers = chatGroupCache?.get(chatGroupMember.chatGroup.id) as? MutableList<RPKChatGroupMember> ?: mutableListOf()
        chatGroupMember.let { chatGroupMembers.remove(it) }
        chatGroupCache?.put(chatGroupMember.chatGroup.id, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(chatGroupMember.minecraftProfile.id) as? MutableList<RPKChatGroupMember>
                ?: mutableListOf()
        chatGroupMember.let { minecraftProfileMembers.remove(it) }
        minecraftProfileCache?.put(chatGroupMember.minecraftProfile.id, minecraftProfileMembers)
    }

}