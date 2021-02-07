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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupId
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupImpl
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupName
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
                .values(entity.name.value)
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKChatGroupId(id)
        cache?.set(id, entity)
        nameCache?.set(entity.name.value, id)
    }

    fun update(entity: RPKChatGroup) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_CHAT_GROUP)
                .set(RPKIT_CHAT_GROUP.NAME, entity.name.value)
                .where(RPKIT_CHAT_GROUP.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
        nameCache?.set(entity.name.value, id.value)
    }

    operator fun get(id: RPKChatGroupId): RPKChatGroup? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        } else {
            val result = database.create
                    .select(RPKIT_CHAT_GROUP.NAME)
                    .from(RPKIT_CHAT_GROUP)
                    .where(RPKIT_CHAT_GROUP.ID.eq(id.value))
                    .fetchOne() ?: return null
            val chatGroup = RPKChatGroupImpl(
                    plugin,
                    RPKChatGroupId(id.value),
                    RPKChatGroupName(result.get(RPKIT_CHAT_GROUP.NAME))
            )
            cache?.set(id.value, chatGroup)
            nameCache?.set(chatGroup.name.value, id.value)
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
    operator fun get(name: RPKChatGroupName): RPKChatGroup? {
        if (nameCache?.containsKey(name.value) == true) {
            val chatGroupId = nameCache[name.value]
            if (chatGroupId != null) {
                return get(RPKChatGroupId(chatGroupId))
            }
        }
        val result = database.create
            .select(RPKIT_CHAT_GROUP.ID)
            .from(RPKIT_CHAT_GROUP)
            .where(RPKIT_CHAT_GROUP.NAME.eq(name.value))
            .fetchOne() ?: return null
        val id = result.get(RPKIT_CHAT_GROUP.ID)
        val chatGroup = RPKChatGroupImpl(
            plugin,
            RPKChatGroupId(id),
            RPKChatGroupName(name.value)
        )
        cache?.set(id, chatGroup)
        nameCache?.set(name.value, id)
        return chatGroup
    }

    fun delete(entity: RPKChatGroup) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_CHAT_GROUP)
                .where(RPKIT_CHAT_GROUP.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
        nameCache?.remove(entity.name.value)
    }
}