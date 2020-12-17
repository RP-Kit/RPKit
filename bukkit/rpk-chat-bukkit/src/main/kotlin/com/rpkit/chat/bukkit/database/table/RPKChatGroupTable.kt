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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupImpl
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_GROUP
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table

/**
 * Represents the chat group table.
 */
class RPKChatGroupTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_chat_group.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-chat-bukkit.rpkit_chat_group.id",
            Int::class.javaObjectType,
            RPKChatGroup::class.java,
            plugin.config.getLong("caching.rpkit_chat_group.id.size")
        )
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_chat_group.name.enabled")) {
        database.cacheManager.createCache(
            "rpk-chat-bukkit.rpkit_chat_group.name",
            String::class.java,
            Int::class.javaObjectType,
            plugin.config.getLong("caching.rpkit_chat_group.name.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKChatGroup) {
        database.create
                .insertInto(
                        RPKIT_CHAT_GROUP,
                        RPKIT_CHAT_GROUP.NAME
                )
                .values(entity.name)
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.set(id, entity)
        nameCache?.set(entity.name, id)
    }

    fun update(entity: RPKChatGroup) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_CHAT_GROUP)
                .set(RPKIT_CHAT_GROUP.NAME, entity.name)
                .where(RPKIT_CHAT_GROUP.ID.eq(id))
                .execute()
        cache?.set(id, entity)
        nameCache?.set(entity.name, id)
    }

    operator fun get(id: Int): RPKChatGroup? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(RPKIT_CHAT_GROUP.NAME)
                    .from(RPKIT_CHAT_GROUP)
                    .where(RPKIT_CHAT_GROUP.ID.eq(id))
                    .fetchOne() ?: return null
            val chatGroup = RPKChatGroupImpl(
                    plugin,
                    id,
                    result.get(RPKIT_CHAT_GROUP.NAME)
            )
            cache?.set(id, chatGroup)
            nameCache?.set(chatGroup.name, id)
            return chatGroup
        }
    }

    /**
     * Gets a chat group by name.
     * If no chat group exists with the given name, null is returned.
     *
     * @param name The name
     * @return The chat group, or null if there is no chat group with the given name
     */
    operator fun get(name: String): RPKChatGroup? {
        if (nameCache?.containsKey(name) == true) {
            val chatGroupId = nameCache[name]
            if (chatGroupId != null) {
                return get(chatGroupId)
            }
        }
        val result = database.create
            .select(RPKIT_CHAT_GROUP.ID)
            .from(RPKIT_CHAT_GROUP)
            .where(RPKIT_CHAT_GROUP.NAME.eq(name))
            .fetchOne() ?: return null
        val id = result.get(RPKIT_CHAT_GROUP.ID)
        val chatGroup = RPKChatGroupImpl(
            plugin,
            id,
            name
        )
        cache?.set(id, chatGroup)
        nameCache?.set(name, id)
        return chatGroup
    }

    fun delete(entity: RPKChatGroup) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_CHAT_GROUP)
                .where(RPKIT_CHAT_GROUP.ID.eq(id))
                .execute()
        cache?.remove(id)
        nameCache?.remove(entity.name)
    }
}