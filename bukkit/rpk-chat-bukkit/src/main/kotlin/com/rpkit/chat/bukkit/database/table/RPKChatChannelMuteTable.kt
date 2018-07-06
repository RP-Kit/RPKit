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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.chat.bukkit.database.jooq.rpkit.Tables.RPKIT_CHAT_CHANNEL_MUTE
import com.rpkit.chat.bukkit.mute.RPKChatChannelMute
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the chat channel mute table
 */
class RPKChatChannelMuteTable(database: Database, private val plugin: RPKChatBukkit): Table<RPKChatChannelMute>(database, RPKChatChannelMute::class) {

    private val cache = database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_channel_mute.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKChatChannelMute::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CHAT_CHANNEL_MUTE)
                .column(RPKIT_CHAT_CHANNEL_MUTE.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_chat_channel_mute").primaryKey(RPKIT_CHAT_CHANNEL_MUTE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.create
                    .truncate(RPKIT_CHAT_CHANNEL_MUTE)
                    .execute()
            database.create.alterTable(RPKIT_CHAT_CHANNEL_MUTE)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create.alterTable(RPKIT_CHAT_CHANNEL_MUTE)
                    .addColumn(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKChatChannelMute): Int {
        database.create
                .insertInto(
                        RPKIT_CHAT_CHANNEL_MUTE,
                        RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID,
                        RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_ID
                )
                .values(
                        entity.minecraftProfile.id,
                        entity.chatChannel.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKChatChannelMute) {
        database.create
                .update(RPKIT_CHAT_CHANNEL_MUTE)
                .set(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_ID, entity.chatChannel.id)
                .where(RPKIT_CHAT_CHANNEL_MUTE.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKChatChannelMute? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID,
                            RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_ID
                    )
                    .from(RPKIT_CHAT_CHANNEL_MUTE)
                    .where(RPKIT_CHAT_CHANNEL_MUTE.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
            val chatChannelId = result.get(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_ID)
            val chatChannel = chatChannelProvider.getChatChannel(chatChannelId)
            if (minecraftProfile != null && chatChannel != null) {
                val chatChannelMute = RPKChatChannelMute(
                        id,
                        minecraftProfile,
                        chatChannel
                )
                cache.put(id, chatChannelMute)
                return chatChannelMute
            } else {
                database.create
                        .deleteFrom(RPKIT_CHAT_CHANNEL_MUTE)
                        .where(RPKIT_CHAT_CHANNEL_MUTE.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets the chat channel mute instance for a Minecraft profile in a channel, or null if there is none.
     *
     * @param minecraftProfile The Minecraft profile
     * @param chatChannel The chat channel
     * @return A chat channel mute instance, or null if none exists
     */
    fun get(minecraftProfile: RPKMinecraftProfile, chatChannel: RPKChatChannel): RPKChatChannelMute? {
        val result = database.create
                .select(RPKIT_CHAT_CHANNEL_MUTE.ID)
                .from(RPKIT_CHAT_CHANNEL_MUTE)
                .where(RPKIT_CHAT_CHANNEL_MUTE.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .and(RPKIT_CHAT_CHANNEL_MUTE.CHAT_CHANNEL_ID.eq(chatChannel.id))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_CHAT_CHANNEL_MUTE.ID))
    }

    override fun delete(entity: RPKChatChannelMute) {
        database.create
                .deleteFrom(RPKIT_CHAT_CHANNEL_MUTE)
                .where(RPKIT_CHAT_CHANNEL_MUTE.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }


}
