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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_CHANNEL_MUTE
import com.rpkit.chat.bukkit.mute.RPKChatChannelMute
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile

/**
 * Represents the chat channel mute table
 */
class RPKChatChannelMuteTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private data class MinecraftProfileChatChannelCacheKey(
        val minecraftProfileId: Int,
        val chatChannelName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_chat_channel_mute.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-chat-bukkit.rpkit_chat_channel_mute.minecraft_profile_id",
            MinecraftProfileChatChannelCacheKey::class.java,
            RPKChatChannelMute::class.java,
            plugin.config.getLong("caching.rpkit_chat_channel_mute.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKChatChannelMute) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        val chatChannelName = entity.chatChannel.name
        database.create
                .insertInto(
                        RPKIT_CHAT_CHANNEL_MUTE,
                        RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME
                )
                .values(
                        minecraftProfileId,
                        chatChannelName.value
                )
                .execute()
        cache?.set(MinecraftProfileChatChannelCacheKey(minecraftProfileId, chatChannelName.value), entity)
    }

    fun get(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel): RPKChatChannelMute? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        val chatChannelName = chatChannel.name
        val cacheKey = MinecraftProfileChatChannelCacheKey(minecraftProfileId, chatChannelName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return cache[cacheKey]
        }
        database.create
                .select(
                        RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME
                )
                .from(RPKIT_CHAT_CHANNEL_MUTE)
                .where(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID.eq(minecraftProfileId))
                .and(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME.eq(chatChannelName.value))
                .fetchOne() ?: return null
        val chatChannelMute = RPKChatChannelMute(
                minecraftProfile,
                chatChannel
        )
        cache?.set(cacheKey, chatChannelMute)
        return chatChannelMute
    }

    fun delete(entity: RPKChatChannelMute) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        val chatChannelName = entity.chatChannel.name
        val cacheKey = MinecraftProfileChatChannelCacheKey(minecraftProfileId, chatChannelName.value)
        database.create
            .deleteFrom(RPKIT_CHAT_CHANNEL_MUTE)
            .where(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID.eq(minecraftProfileId))
            .and(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_NAME.eq(chatChannelName.value))
            .execute()
        cache?.remove(cacheKey)
    }

}
