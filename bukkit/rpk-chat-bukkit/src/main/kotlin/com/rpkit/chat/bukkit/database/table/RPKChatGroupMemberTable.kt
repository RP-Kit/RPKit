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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupId
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupMember
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_GROUP_MEMBER
import com.rpkit.core.caching.RPKCacheConfiguration
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level.SEVERE

/**
 * Represents the chat group member table.
 */
class RPKChatGroupMemberTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val chatGroupCache = if (plugin.config.getBoolean("caching.rpkit_chat_group_member.chat_group_id.enabled")) {
        database.cacheManager.createCache(
            RPKCacheConfiguration<Int, MutableList<RPKChatGroupMember>>(
                "rpk-chat-bukkit.rpkit_chat_group_member.chat_group_id",
                plugin.config.getLong("caching.rpkit_chat_group_member.chat_group_id.size")
            )
        )
    } else {
        null
    }

    private val minecraftProfileCache = if (plugin.config.getBoolean("caching.rpkit_chat_group_member.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            RPKCacheConfiguration<Int, MutableList<RPKChatGroupMember>>(
                "rpk-chat-bukkit.rpkit_chat_group_member.minecraft_profile_id",
                plugin.config.getLong("caching.rpkit_chat_group_member.minecraft_profile_id.size")
            )
        )
    } else {
        null
    }

    fun insert(entity: RPKChatGroupMember): CompletableFuture<Void> {
        val chatGroupId = entity.chatGroup.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_CHAT_GROUP_MEMBER,
                    RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID,
                    RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID
                )
                .values(
                    chatGroupId.value,
                    minecraftProfileId.value
                )
                .execute()
            cache(entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to insert chat group member", exception)
            throw exception
        }
    }

    /**
     * Gets a list of members of a specific chat group.
     *
     * @param chatGroup The chat group
     * @return A list of members of the chat group
     */
    fun get(chatGroup: RPKChatGroup): CompletableFuture<List<RPKChatGroupMember>> {
        val chatGroupId = chatGroup.id ?: return CompletableFuture.completedFuture(emptyList())
        return if (chatGroupCache?.containsKey(chatGroupId.value) == true) {
            CompletableFuture.completedFuture(chatGroupCache[chatGroupId.value] as List<RPKChatGroupMember>)
        } else {
            CompletableFuture.supplyAsync {
                val results = database.create
                    .select(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_CHAT_GROUP_MEMBER)
                    .where(RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID.eq(chatGroupId.value))
                    .fetch()
                val chatGroupMembers = results.mapNotNull { result ->
                    val minecraftProfileService =
                        Services[RPKMinecraftProfileService::class.java] ?: return@mapNotNull null
                    val minecraftProfile = minecraftProfileService
                        .getMinecraftProfile(RPKMinecraftProfileId(result[RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID]))
                        .join()
                        ?: return@mapNotNull null
                    RPKChatGroupMember(
                        chatGroup,
                        minecraftProfile
                    )
                }
                chatGroupCache?.set(chatGroupId.value, chatGroupMembers.toMutableList())
                return@supplyAsync chatGroupMembers
            }.exceptionally { exception ->
                plugin.logger.log(SEVERE, "Failed to get chat group", exception)
                throw exception
            }
        }
    }

    /**
     * Gets a list of chat group member instances for a Minecraft profile
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group member instances
     */
    fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<List<RPKChatGroupMember>> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(emptyList())
        return if (minecraftProfileCache?.containsKey(minecraftProfileId.value) == true) {
            CompletableFuture.completedFuture(minecraftProfileCache[minecraftProfileId.value] as List<RPKChatGroupMember>)
        } else {
            CompletableFuture.supplyAsync {
                val results = database.create
                    .select(RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID)
                    .from(RPKIT_CHAT_GROUP_MEMBER)
                    .where(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                    .fetch()
                val chatGroupMembers = results.mapNotNull { result ->
                    val chatGroupService = Services[RPKChatGroupService::class.java] ?: return@mapNotNull null
                    val chatGroup = chatGroupService
                        .getChatGroup(RPKChatGroupId(result[RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID])).join()
                        ?: return@mapNotNull null
                    RPKChatGroupMember(
                        chatGroup,
                        minecraftProfile
                    )
                }
                minecraftProfileCache?.set(minecraftProfileId.value, chatGroupMembers.toMutableList())
                return@supplyAsync chatGroupMembers
            }.exceptionally { exception ->
                plugin.logger.log(SEVERE, "Failed to get chat group members", exception)
                throw exception
            }
        }
    }

    fun delete(entity: RPKChatGroupMember): CompletableFuture<Void> {
        val chatGroupId = entity.chatGroup.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_CHAT_GROUP_MEMBER)
                .where(RPKIT_CHAT_GROUP_MEMBER.CHAT_GROUP_ID.eq(chatGroupId.value))
                .and(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            uncache(entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to delete chat group member", exception)
            throw exception
        }
    }

    fun delete(minecraftProfileId: RPKMinecraftProfileId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_CHAT_GROUP_MEMBER)
            .where(RPKIT_CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .execute()
        chatGroupCache?.removeMatching { members ->
            members.any { member -> member.minecraftProfile.id?.value == minecraftProfileId.value }
        }
        minecraftProfileCache?.remove(minecraftProfileId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete chat group members for Minecraft profile id", exception)
        throw exception
    }

    private fun cache(chatGroupMember: RPKChatGroupMember) {
        val chatGroupId = chatGroupMember.chatGroup.id ?: return
        val minecraftProfileId = chatGroupMember.minecraftProfile.id ?: return
        val chatGroupMembers = chatGroupCache?.get(chatGroupId.value) ?: mutableListOf()
        if (!chatGroupMember.let { chatGroupMembers.contains(it) }) {
            chatGroupMember.let { chatGroupMembers.add(it) }
        }
        chatGroupCache?.set(chatGroupId.value, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(minecraftProfileId.value)
                ?: mutableListOf()
        if (!chatGroupMember.let { minecraftProfileMembers.contains(it) }) {
            chatGroupMember.let { minecraftProfileMembers.add(it) }
        }
        minecraftProfileCache?.set(minecraftProfileId.value, minecraftProfileMembers)
    }

    private fun uncache(chatGroupMember: RPKChatGroupMember) {
        val chatGroupId = chatGroupMember.chatGroup.id ?: return
        val minecraftProfileId = chatGroupMember.minecraftProfile.id ?: return
        val chatGroupMembers = chatGroupCache?.get(chatGroupId.value) ?: mutableListOf()
        chatGroupMember.let { chatGroupMembers.remove(it) }
        chatGroupCache?.set(chatGroupId.value, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(minecraftProfileId.value)
                ?: mutableListOf()
        chatGroupMember.let { minecraftProfileMembers.remove(it) }
        minecraftProfileCache?.set(minecraftProfileId.value, minecraftProfileMembers)
    }

}