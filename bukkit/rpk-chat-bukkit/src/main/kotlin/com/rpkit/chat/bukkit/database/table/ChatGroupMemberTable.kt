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
import com.rpkit.chat.bukkit.chatgroup.ChatGroupMember
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
import com.rpkit.chat.bukkit.database.jooq.rpkit.Tables.CHAT_GROUP_MEMBER
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType

/**
 * Represents the chat group member table.
 */
class ChatGroupMemberTable(database: Database, private val plugin: RPKChatBukkit): Table<ChatGroupMember>(database, ChatGroupMember::class) {

    private val cache = if (plugin.config.getBoolean("caching.chat_group_member.id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.chat_group_member.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatGroupMember::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.chat_group_member.id.size"))).build())
    } else {
        null
    }

    private val chatGroupCache = if (plugin.config.getBoolean("caching.chat_group_member.chat_group_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.chat_group_member.chat_group_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.chat_group_member.chat_group_id.size"))).build())
    } else {
        null
    }

    private val minecraftProfileCache = if(plugin.config.getBoolean("caching.chat_group_member.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.chat_group_member.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.chat_group_member.minecraft_profile_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(CHAT_GROUP_MEMBER)
                .column(CHAT_GROUP_MEMBER.ID, SQLDataType.INTEGER.identity(true))
                .column(CHAT_GROUP_MEMBER.CHAT_GROUP_ID, SQLDataType.INTEGER)
                .column(CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_chat_group_member").primaryKey(CHAT_GROUP_MEMBER.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.create
                    .truncate(CHAT_GROUP_MEMBER)
                    .execute()
            database.create
                    .alterTable(CHAT_GROUP_MEMBER)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(CHAT_GROUP_MEMBER)
                    .addColumn(CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: ChatGroupMember): Int {
        database.create
                .insertInto(
                        CHAT_GROUP_MEMBER,
                        CHAT_GROUP_MEMBER.CHAT_GROUP_ID,
                        CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.chatGroup.id,
                        entity.minecraftProfile.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        val chatGroupMembers = chatGroupCache?.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf()
        if (!chatGroupMembers.contains(entity.id)) {
            chatGroupMembers.add(entity.id)
        }
        chatGroupCache?.put(entity.chatGroup.id, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf()
        if (!minecraftProfileMembers.contains(entity.id)) {
            minecraftProfileMembers.add(entity.id)
        }
        minecraftProfileCache?.put(entity.minecraftProfile.id, minecraftProfileMembers)
        return id
    }

    override fun update(entity: ChatGroupMember) {
        database.create
                .update(CHAT_GROUP_MEMBER)
                .set(CHAT_GROUP_MEMBER.CHAT_GROUP_ID, entity.chatGroup.id)
                .set(CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .where(CHAT_GROUP_MEMBER.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        val chatGroupMembers = chatGroupCache?.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf()
        if (!chatGroupMembers.contains(entity.id)) {
            chatGroupMembers.add(entity.id)
        }
        chatGroupCache?.put(entity.chatGroup.id, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf()
        if (!minecraftProfileMembers.contains(entity.id)) {
            minecraftProfileMembers.add(entity.id)
        }
        minecraftProfileCache?.put(entity.minecraftProfile.id, minecraftProfileMembers)
    }

    override fun get(id: Int): ChatGroupMember? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            CHAT_GROUP_MEMBER.CHAT_GROUP_ID,
                            CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID
                    )
                    .from(CHAT_GROUP_MEMBER)
                    .where(CHAT_GROUP_MEMBER.ID.eq(id))
                    .fetchOne() ?: return null
            val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class)
            val chatGroupId = result.get(CHAT_GROUP_MEMBER.CHAT_GROUP_ID)
            val chatGroup = chatGroupProvider.getChatGroup(chatGroupId)
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            if (chatGroup != null && minecraftProfile != null) {
                val chatGroupMember = ChatGroupMember(
                        id,
                        chatGroup,
                        minecraftProfile
                )
                cache?.put(chatGroupMember.id, chatGroupMember)
                val chatGroupMembers = chatGroupCache?.get(chatGroupMember.chatGroup.id) as? MutableList<Int> ?: mutableListOf()
                if (!chatGroupMembers.contains(chatGroupMember.id)) {
                    chatGroupMembers.add(chatGroupMember.id)
                }
                chatGroupCache?.put(chatGroupMember.chatGroup.id, chatGroupMembers)
                val minecraftProfileMembers = minecraftProfileCache?.get(chatGroupMember.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf()
                if (!minecraftProfileMembers.contains(chatGroupMember.id)) {
                    minecraftProfileMembers.add(chatGroupMember.id)
                }
                minecraftProfileCache?.put(chatGroupMember.minecraftProfile.id, minecraftProfileMembers)
                return chatGroupMember
            } else {
                database.create
                        .deleteFrom(CHAT_GROUP_MEMBER)
                        .where(CHAT_GROUP_MEMBER.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets a list of members of a specific chat group.
     *
     * @param chatGroup The chat group
     * @return A list of members of the chat group
     */
    fun get(chatGroup: RPKChatGroup): List<ChatGroupMember> {
        return if (chatGroupCache?.containsKey(chatGroup.id) == true) {
            (chatGroupCache.get(chatGroup.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val results = database.create
                    .select(CHAT_GROUP_MEMBER.ID)
                    .from(CHAT_GROUP_MEMBER)
                    .where(CHAT_GROUP_MEMBER.CHAT_GROUP_ID.eq(chatGroup.id))
                    .fetch()
            val chatGroupMembers = results.map { result ->
                get(result.get(CHAT_GROUP_MEMBER.ID))
            }.filterNotNull()
            chatGroupCache?.put(chatGroup.id, chatGroupMembers.map(ChatGroupMember::id).toMutableList())
            chatGroupMembers
        }
    }

    /**
     * Gets a list of chat group member instances for a Minecraft profile
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group member instances
     */
    fun get(minecraftProfile: RPKMinecraftProfile): List<ChatGroupMember> {
        return if (minecraftProfileCache?.containsKey(minecraftProfile.id) == true) {
            (minecraftProfileCache.get(minecraftProfile.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val results = database.create
                    .select(CHAT_GROUP_MEMBER.ID)
                    .from(CHAT_GROUP_MEMBER)
                    .where(CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetch()
            val chatGroupMembers = results.map { result ->
                get(result.get(CHAT_GROUP_MEMBER.ID))
            }.filterNotNull()
            minecraftProfileCache?.put(minecraftProfile.id, chatGroupMembers.map(ChatGroupMember::id).toMutableList())
            chatGroupMembers
        }
    }

    override fun delete(entity: ChatGroupMember) {
        database.create
                .deleteFrom(CHAT_GROUP_MEMBER)
                .where(CHAT_GROUP_MEMBER.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        val chatGroupMembers = chatGroupCache?.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf()
        chatGroupMembers.remove(entity.id)
        chatGroupCache?.put(entity.chatGroup.id, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf()
        minecraftProfileMembers.remove(entity.id)
        minecraftProfileCache?.put(entity.minecraftProfile.id, minecraftProfileMembers)
    }

}