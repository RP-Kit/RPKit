/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.chat.bukkit.chatgroup.ChatGroupInvite
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
import com.rpkit.chat.bukkit.database.jooq.rpkit.Tables.CHAT_GROUP_INVITE
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
 * Represents chat group invite table.
 */
class ChatGroupInviteTable(database: Database, private val plugin: RPKChatBukkit): Table<ChatGroupInvite>(database, ChatGroupInvite::class) {

    private val cache = if (plugin.config.getBoolean("caching.chat_group_invite.id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.chat_group_invite.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatGroupInvite::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.chat_group_invite.id.size"))).build())
    } else {
        null
    }

    private val chatGroupCache = if (plugin.config.getBoolean("caching.chat_group_invite.chat_group_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.chat_group_invite.chat_group_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.chat_group_invite.chat_group_id.size"))).build())
    } else {
        null
    }

    private val minecraftProfileCache = if (plugin.config.getBoolean("caching.chat_group_invite.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.chat_group_invite.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.chat_group_invite.minecraft_profile_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(CHAT_GROUP_INVITE)
                .column(CHAT_GROUP_INVITE.ID, SQLDataType.INTEGER.identity(true))
                .column(CHAT_GROUP_INVITE.CHAT_GROUP_ID, SQLDataType.INTEGER)
                .column(CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_chat_group_invite").primaryKey(CHAT_GROUP_INVITE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.create
                    .truncate(CHAT_GROUP_INVITE)
                    .execute()
            database.create
                    .alterTable(CHAT_GROUP_INVITE)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(CHAT_GROUP_INVITE)
                    .addColumn(CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: ChatGroupInvite): Int {
        database.create
                .insertInto(
                        CHAT_GROUP_INVITE,
                        CHAT_GROUP_INVITE.CHAT_GROUP_ID,
                        CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.chatGroup.id,
                        entity.minecraftProfile.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        val chatGroupInvites = chatGroupCache?.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
        if (!chatGroupInvites.contains(entity.minecraftProfile.id)) {
            chatGroupInvites.add(id)
        }
        chatGroupCache?.put(entity.chatGroup.id, chatGroupInvites)
        val minecraftProfileInvites = minecraftProfileCache?.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
        if (!minecraftProfileInvites.contains(entity.minecraftProfile.id)) {
            minecraftProfileInvites.add(id)
        }
        minecraftProfileCache?.put(entity.minecraftProfile.id, minecraftProfileInvites)
        return id
    }

    override fun update(entity: ChatGroupInvite) {
        database.create
                .update(CHAT_GROUP_INVITE)
                .set(CHAT_GROUP_INVITE.CHAT_GROUP_ID, entity.chatGroup.id)
                .set(CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .where(CHAT_GROUP_INVITE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        val chatGroupInvites = chatGroupCache?.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
        if (!chatGroupInvites.contains(entity.id)) {
            chatGroupInvites.add(entity.id)
        }
        chatGroupCache?.put(entity.chatGroup.id, chatGroupInvites)
        val minecraftProfileInvites = minecraftProfileCache?.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
        if (!minecraftProfileInvites.contains(entity.minecraftProfile.id)) {
            minecraftProfileInvites.add(entity.id)
        }
        minecraftProfileCache?.put(entity.minecraftProfile.id, minecraftProfileInvites)
    }

    override fun get(id: Int): ChatGroupInvite? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            CHAT_GROUP_INVITE.CHAT_GROUP_ID,
                            CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID
                    )
                    .from(CHAT_GROUP_INVITE)
                    .where(CHAT_GROUP_INVITE.ID.eq(id))
                    .fetchOne() ?: return null
            val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class)
            val chatGroupId = result.get(CHAT_GROUP_INVITE.CHAT_GROUP_ID)
            val chatGroup = chatGroupProvider.getChatGroup(chatGroupId)
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            if (chatGroup != null && minecraftProfile != null) {
                val chatGroupInvite = ChatGroupInvite(
                        id,
                        chatGroup,
                        minecraftProfile
                )
                cache?.put(id, chatGroupInvite)
                val chatGroupInvites = chatGroupCache?.get(chatGroupInvite.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
                if (!chatGroupInvites.contains(chatGroupInvite.id)) {
                    chatGroupInvites.add(chatGroupInvite.id)
                }
                chatGroupCache?.put(chatGroupInvite.chatGroup.id, chatGroupInvites)
                val minecraftProfileInvites = minecraftProfileCache?.get(chatGroupInvite.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
                if (!minecraftProfileInvites.contains(chatGroupInvite.minecraftProfile.id)) {
                    minecraftProfileInvites.add(chatGroupInvite.id)
                }
                minecraftProfileCache?.put(chatGroupInvite.minecraftProfile.id, minecraftProfileInvites)
                return chatGroupInvite
            } else {
                database.create
                        .deleteFrom(CHAT_GROUP_INVITE)
                        .where(CHAT_GROUP_INVITE.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets a list of invites for a particular chat group.
     *
     * @param chatGroup The chat group
     * @return A list of chat group invites
     */
    fun get(chatGroup: RPKChatGroup): List<ChatGroupInvite> {
        if (chatGroupCache?.containsKey(chatGroup.id) == true) {
            return (chatGroupCache.get(chatGroup.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val results = database.create
                    .select(CHAT_GROUP_INVITE.ID)
                    .from(CHAT_GROUP_INVITE)
                    .where(CHAT_GROUP_INVITE.CHAT_GROUP_ID.eq(chatGroup.id))
                    .fetch()
            val chatGroupInvites = results.map { result ->
                get(result.get(CHAT_GROUP_INVITE.ID))
            }.filterNotNull()
            chatGroupCache?.put(chatGroup.id, chatGroupInvites.map(ChatGroupInvite::id).toMutableList())
            return chatGroupInvites
        }
    }

    /**
     * Gets a list of chat group invites for a particular Minecraft profile.
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group invites for the Minecraft profile
     */
    fun get(minecraftProfile: RPKMinecraftProfile): List<ChatGroupInvite> {
        if (minecraftProfileCache?.containsKey(minecraftProfile.id) == true) {
            return (minecraftProfileCache.get(minecraftProfile.id) as List<Int>).map { id -> get(id)!! }
        } else {
            val results = database.create
                    .select(CHAT_GROUP_INVITE.ID)
                    .from(CHAT_GROUP_INVITE)
                    .where(CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetch()
            val chatGroupInvites = results.map { result ->
                get(result.get(CHAT_GROUP_INVITE.ID))
            }.filterNotNull()
            minecraftProfileCache?.put(minecraftProfile.id, chatGroupInvites.map(ChatGroupInvite::id).toMutableList())
            return chatGroupInvites
        }
    }

    override fun delete(entity: ChatGroupInvite) {
        database.create
                .deleteFrom(CHAT_GROUP_INVITE)
                .where(CHAT_GROUP_INVITE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        val chatGroupMembers = chatGroupCache?.get(entity.chatGroup.id) as? MutableList<Int> ?: mutableListOf<Int>()
        chatGroupMembers.remove(entity.id)
        chatGroupCache?.put(entity.chatGroup.id, chatGroupMembers)
        val minecraftProfileMembers = minecraftProfileCache?.get(entity.minecraftProfile.id) as? MutableList<Int> ?: mutableListOf<Int>()
        minecraftProfileMembers.remove(entity.id)
        minecraftProfileCache?.put(entity.minecraftProfile.id, minecraftProfileMembers)
    }

}