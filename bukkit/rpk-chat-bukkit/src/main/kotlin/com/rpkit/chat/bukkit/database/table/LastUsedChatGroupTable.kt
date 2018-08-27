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
import com.rpkit.chat.bukkit.chatgroup.LastUsedChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
import com.rpkit.chat.bukkit.database.jooq.rpkit.Tables.LAST_USED_CHAT_GROUP
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
 * Represents the last used chat group table
 */
class LastUsedChatGroupTable(database: Database, private val plugin: RPKChatBukkit): Table<LastUsedChatGroup>(database, LastUsedChatGroup::class) {

    private val cache = if (plugin.config.getBoolean("caching.last_used_chat_group.id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.last_used_chat_group.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, LastUsedChatGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.last_used_chat_group.id.size"))))
    } else {
        null
    }

    private val minecraftProfileCache = if (plugin.config.getBoolean("caching.last_used_chat_group.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.last_used_chat_group.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.last_used_chat_group.minecraft_profile_id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(LAST_USED_CHAT_GROUP)
                .column(LAST_USED_CHAT_GROUP.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(LAST_USED_CHAT_GROUP.CHAT_GROUP_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_last_used_chat_group").primaryKey(LAST_USED_CHAT_GROUP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.4.0") {
            database.create
                    .truncate(LAST_USED_CHAT_GROUP)
                    .execute()
            database.create
                    .alterTable(LAST_USED_CHAT_GROUP)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(LAST_USED_CHAT_GROUP)
                    .addColumn(LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: LastUsedChatGroup): Int {
        database.create
                .insertInto(
                        LAST_USED_CHAT_GROUP,
                        LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID,
                        LAST_USED_CHAT_GROUP.CHAT_GROUP_ID
                )
                .values(
                        entity.minecraftProfile.id,
                        entity.chatGroup.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        minecraftProfileCache?.put(entity.minecraftProfile.id, id)
        return id
    }

    override fun update(entity: LastUsedChatGroup) {
        database.create
                .update(LAST_USED_CHAT_GROUP)
                .set(LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(LAST_USED_CHAT_GROUP.CHAT_GROUP_ID, entity.chatGroup.id)
                .where(LAST_USED_CHAT_GROUP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        minecraftProfileCache?.put(entity.minecraftProfile.id, entity.id)
    }

    override fun get(id: Int): LastUsedChatGroup? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID,
                            LAST_USED_CHAT_GROUP.CHAT_GROUP_ID
                    )
                    .from(LAST_USED_CHAT_GROUP)
                    .where(LAST_USED_CHAT_GROUP.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class)
            val chatGroupId = result.get(LAST_USED_CHAT_GROUP.CHAT_GROUP_ID)
            val chatGroup = chatGroupProvider.getChatGroup(chatGroupId)
            if (minecraftProfile != null && chatGroup != null) {
                val lastUsedChatGroup = LastUsedChatGroup(
                        id,
                        minecraftProfile,
                        chatGroup
                )
                cache?.put(id, lastUsedChatGroup)
                minecraftProfileCache?.put(lastUsedChatGroup.minecraftProfile.id, id)
                return lastUsedChatGroup
            } else {
                database.create
                        .deleteFrom(LAST_USED_CHAT_GROUP)
                        .where(LAST_USED_CHAT_GROUP.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets the last used chat group of a minecraftProfile.
     * If the minecraftProfile has never used a chat group, null is returned.
     *
     * @param minecraftProfile The minecraftProfile
     * @return The minecraftProfile's last used chat group, or null if no chat group has been used
     */
    fun get(minecraftProfile: RPKMinecraftProfile): LastUsedChatGroup? {
        if (minecraftProfileCache?.containsKey(minecraftProfile.id) == true) {
            return get(minecraftProfileCache.get(minecraftProfile.id))
        } else {
            val result = database.create
                    .select(LAST_USED_CHAT_GROUP.ID)
                    .from(LAST_USED_CHAT_GROUP)
                    .where(LAST_USED_CHAT_GROUP.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                    .fetchOne() ?: return null
            return get(result.get(LAST_USED_CHAT_GROUP.ID))
        }
    }

    override fun delete(entity: LastUsedChatGroup) {
        database.create
                .deleteFrom(LAST_USED_CHAT_GROUP)
                .where(LAST_USED_CHAT_GROUP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        minecraftProfileCache?.remove(entity.minecraftProfile.id)
    }

}