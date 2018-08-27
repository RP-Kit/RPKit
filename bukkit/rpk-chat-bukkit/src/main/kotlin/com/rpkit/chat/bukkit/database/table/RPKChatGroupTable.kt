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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupImpl
import com.rpkit.chat.bukkit.database.jooq.rpkit.Tables.RPKIT_CHAT_GROUP
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the chat group table.
 */
class RPKChatGroupTable(database: Database, private val plugin: RPKChatBukkit): Table<RPKChatGroup>(database, RPKChatGroup::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_chat_group.id.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_group.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKChatGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_chat_group.id.size"))))
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_chat_group.name.enabled")) {
        database.cacheManager.createCache("rpk-chat-bukkit.rpkit_chat_group.name",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_chat_group.name.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CHAT_GROUP)
                .column(RPKIT_CHAT_GROUP.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHAT_GROUP.NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_chat_group").primaryKey(RPKIT_CHAT_GROUP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKChatGroup): Int {
        database.create
                .insertInto(
                        RPKIT_CHAT_GROUP,
                        RPKIT_CHAT_GROUP.NAME
                )
                .values(entity.name)
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        nameCache?.put(entity.name, id)
        return id
    }

    override fun update(entity: RPKChatGroup) {
        database.create
                .update(RPKIT_CHAT_GROUP)
                .set(RPKIT_CHAT_GROUP.NAME, entity.name)
                .where(RPKIT_CHAT_GROUP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        nameCache?.put(entity.name, entity.id)
    }

    override fun get(id: Int): RPKChatGroup? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
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
            cache?.put(id, chatGroup)
            nameCache?.put(chatGroup.name, id)
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
    fun get(name: String): RPKChatGroup? {
        if (nameCache?.containsKey(name) == true) {
            return get(nameCache.get(name))
        } else {
            val result = database.create
                    .select(RPKIT_CHAT_GROUP.ID)
                    .from(RPKIT_CHAT_GROUP)
                    .where(RPKIT_CHAT_GROUP.NAME.eq(name))
                    .fetchOne() ?: return null
            val chatGroup = RPKChatGroupImpl(
                    plugin,
                    result.get(RPKIT_CHAT_GROUP.ID),
                    name
            )
            cache?.put(chatGroup.id, chatGroup)
            nameCache?.put(name, chatGroup.id)
            return chatGroup
        }
    }

    override fun delete(entity: RPKChatGroup) {
        database.create
                .deleteFrom(RPKIT_CHAT_GROUP)
                .where(RPKIT_CHAT_GROUP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        nameCache?.remove(entity.name)
    }
}