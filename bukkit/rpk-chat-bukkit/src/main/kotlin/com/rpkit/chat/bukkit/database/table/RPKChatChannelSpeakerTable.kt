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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelName
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_CHANNEL_SPEAKER
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeaker
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile

/**
 * Represents the chat channel speaker table
 */
class RPKChatChannelSpeakerTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_chat_channel_speaker.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-chat-bukkit.rpkit_chat_channel_speaker.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKChatChannelSpeaker::class.java,
            plugin.config.getLong("caching.rpkit_chat_channel_speaker.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKChatChannelSpeaker) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .insertInto(
                        RPKIT_CHAT_CHANNEL_SPEAKER,
                        RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME
                )
                .values(
                    minecraftProfileId.value,
                    entity.chatChannel.name.value
                )
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKChatChannelSpeaker? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return cache[minecraftProfileId.value]
        }
        val result = database.create
                .select(
                        RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME
                )
                .from(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetchOne() ?: return null
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return null
        val chatChannel = chatChannelService.getChatChannel(RPKChatChannelName(result[RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME]))
                ?: return null
        val chatChannelSpeaker = RPKChatChannelSpeaker(
                minecraftProfile,
                chatChannel
        )
        cache?.set(minecraftProfileId.value, chatChannelSpeaker)
        return chatChannelSpeaker
    }

    fun update(entity: RPKChatChannelSpeaker) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .update(RPKIT_CHAT_CHANNEL_SPEAKER)
                .set(RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME, entity.chatChannel.name.value)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    fun delete(entity: RPKChatChannelSpeaker) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .deleteFrom(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.remove(minecraftProfileId.value)
    }

}
