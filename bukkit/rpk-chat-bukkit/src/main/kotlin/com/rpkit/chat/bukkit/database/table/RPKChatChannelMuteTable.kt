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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_CHANNEL_MUTE
import com.rpkit.chat.bukkit.mute.RPKChatChannelMute
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the chat channel mute table
 */
class RPKChatChannelMuteTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_chat_channel_mute.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_channel_mute.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_chat_channel_mute.minecraft_profile_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKChatChannelMute) {
        database.create
                .insertInto(
                        RPKIT_CHAT_CHANNEL_MUTE,
                        RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME
                )
                .values(
                        entity.minecraftProfile.id,
                        entity.chatChannel.name
                )
                .execute()
        val chatChannelMutes = cache?.get(entity.minecraftProfile.id) as? MutableMap<String, RPKChatChannelMute> ?: mutableMapOf()
        chatChannelMutes[entity.chatChannel.name] = entity
        cache?.put(entity.minecraftProfile.id, chatChannelMutes)
    }

    fun get(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel): RPKChatChannelMute? {
        if (cache?.containsKey(minecraftProfile.id) == true) {
            val chatChannelMutes = cache[minecraftProfile.id] as MutableMap<String, RPKChatChannelMute>
            if (chatChannelMutes.containsKey(chatChannel.name)) {
                return chatChannelMutes[chatChannel.name]
            }
        }
        database.create
                .select(
                        RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME
                )
                .from(RPKIT_CHAT_CHANNEL_MUTE)
                .where(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .and(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME.eq(chatChannel.name))
                .fetchOne() ?: return null
        val chatChannelMute = RPKChatChannelMute(
                minecraftProfile,
                chatChannel
        )
        val chatChannelMutes = cache?.get(chatChannelMute.minecraftProfile.id) as? MutableMap<String, RPKChatChannelMute>
                ?: mutableMapOf()
        chatChannelMutes[chatChannelMute.chatChannel.name] = chatChannelMute
        cache?.put(chatChannelMute.minecraftProfile.id, chatChannelMutes)
        return chatChannelMute
    }

    fun delete(entity: RPKChatChannelMute) {
        database.create
                .deleteFrom(RPKIT_CHAT_CHANNEL_MUTE)
                .where(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.remove(entity.minecraftProfile.id)
    }

}
