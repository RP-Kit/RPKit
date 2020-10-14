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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_CHANNEL_SPEAKER
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeaker
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the chat channel speaker table
 */
class RPKChatChannelSpeakerTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_chat_channel_speaker.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_channel_speaker.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKChatChannelSpeaker::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_chat_channel_speaker.minecraft_profile_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKChatChannelSpeaker) {
        database.create
                .insertInto(
                        RPKIT_CHAT_CHANNEL_SPEAKER,
                        RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME
                )
                .values(
                        entity.minecraftProfile.id,
                        entity.chatChannel.name
                )
                .execute()
        cache?.put(entity.minecraftProfile.id, entity)
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKChatChannelSpeaker? {
        if (cache?.containsKey(minecraftProfile.id) == true) {
            return cache[minecraftProfile.id]
        }
        val result = database.create
                .select(
                        RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME
                )
                .from(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        val chatChannelService = Services[RPKChatChannelService::class] ?: return null
        val chatChannel = chatChannelService.getChatChannel(result[RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME])
                ?: return null
        val chatChannelSpeaker = RPKChatChannelSpeaker(
                minecraftProfile,
                chatChannel
        )
        cache?.put(chatChannelSpeaker.minecraftProfile.id, chatChannelSpeaker)
        return chatChannelSpeaker
    }

    fun update(entity: RPKChatChannelSpeaker) {
        database.create
                .update(RPKIT_CHAT_CHANNEL_SPEAKER)
                .set(RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME, entity.chatChannel.name)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.put(entity.minecraftProfile.id, entity)
    }

    fun delete(entity: RPKChatChannelSpeaker) {
        database.create
                .deleteFrom(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.remove(entity.minecraftProfile.id)
    }

}
