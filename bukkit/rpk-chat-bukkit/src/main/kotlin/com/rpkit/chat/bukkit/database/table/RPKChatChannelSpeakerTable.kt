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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelName
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_CHANNEL_SPEAKER
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeaker
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level.SEVERE

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

    fun insert(entity: RPKChatChannelSpeaker): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
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
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to insert chat channel speaker", exception)
            throw exception
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKChatChannelSpeaker?> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return CompletableFuture.completedFuture(cache[minecraftProfileId.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME
                )
                .from(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .groupBy(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID)
                .fetchOne() ?: return@supplyAsync null
            val chatChannelService = Services[RPKChatChannelService::class.java] ?: return@supplyAsync null
            val chatChannel =
                chatChannelService.getChatChannel(RPKChatChannelName(result[RPKIT_CHAT_CHANNEL_SPEAKER.CHAT_CHANNEL_NAME]))
                    ?: return@supplyAsync null
            val chatChannelSpeaker = RPKChatChannelSpeaker(
                minecraftProfile,
                chatChannel
            )
            cache?.set(minecraftProfileId.value, chatChannelSpeaker)
            return@supplyAsync chatChannelSpeaker
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to get chat channel speaker", exception)
            throw exception
        }
    }

    fun update(entity: RPKChatChannelSpeaker): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create.deleteFrom(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            database.create.insertInto(RPKIT_CHAT_CHANNEL_SPEAKER)
                .values(
                    minecraftProfileId.value,
                    entity.chatChannel.name.value
                )
                .execute()
            cache?.set(minecraftProfileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to update chat channel speaker", exception)
            throw exception
        }
    }

    fun delete(entity: RPKChatChannelSpeaker): CompletableFuture<Void> {
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_CHAT_CHANNEL_SPEAKER)
                .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
            cache?.remove(minecraftProfileId.value)
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to delete chat channel speaker", exception)
            throw exception
        }
    }

    fun delete(minecraftProfileId: RPKMinecraftProfileId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_CHAT_CHANNEL_SPEAKER)
            .where(RPKIT_CHAT_CHANNEL_SPEAKER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .execute()
        cache?.remove(minecraftProfileId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete chat channel speaker for Minecraft profile id", exception)
        throw exception
    }

}
