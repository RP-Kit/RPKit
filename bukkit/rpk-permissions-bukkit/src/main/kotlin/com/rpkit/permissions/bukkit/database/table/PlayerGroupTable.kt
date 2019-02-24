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

package com.rpkit.permissions.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.jooq.rpkit.Tables.PLAYER_GROUP
import com.rpkit.permissions.bukkit.group.PlayerGroup
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType

/**
 * Represents the player group table.
 */
class PlayerGroupTable(database: Database, private val plugin: RPKPermissionsBukkit) : Table<PlayerGroup>(database, PlayerGroup::class) {

    private val cache = if (plugin.config.getBoolean("caching.player_group.id.enabled")) {
        database.cacheManager.createCache("rpk-permissions-bukkit.player_group.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, PlayerGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.player_group.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(PLAYER_GROUP)
                .column(PLAYER_GROUP.ID, SQLDataType.INTEGER.identity(true))
                .column(PLAYER_GROUP.PLAYER_ID, SQLDataType.INTEGER)
                .column(PLAYER_GROUP.GROUP_NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_player_group").primaryKey(PLAYER_GROUP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: PlayerGroup): Int {
        database.create
                .insertInto(
                        PLAYER_GROUP,
                        PLAYER_GROUP.PLAYER_ID,
                        PLAYER_GROUP.GROUP_NAME
                )
                .values(
                        entity.player.id,
                        entity.group.name
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: PlayerGroup) {
        database.create
                .update(PLAYER_GROUP)
                .set(PLAYER_GROUP.PLAYER_ID, entity.player.id)
                .set(PLAYER_GROUP.GROUP_NAME, entity.group.name)
                .where(PLAYER_GROUP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): PlayerGroup? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            PLAYER_GROUP.PLAYER_ID,
                            PLAYER_GROUP.GROUP_NAME
                    )
                    .from(PLAYER_GROUP)
                    .where(PLAYER_GROUP.ID.eq(id))
                    .fetchOne() ?: return null
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val playerId = result.get(PLAYER_GROUP.PLAYER_ID)
            val player = playerProvider.getPlayer(playerId)
            val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
            val groupName = result.get(PLAYER_GROUP.GROUP_NAME)
            val group = groupProvider.getGroup(groupName)
            if (player != null && group != null) {
                val playerGroup = PlayerGroup(
                        id,
                        player,
                        group
                )
                cache?.put(id, playerGroup)
                return playerGroup
            } else {
                database.create
                        .deleteFrom(PLAYER_GROUP)
                        .where(PLAYER_GROUP.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(player: RPKPlayer): List<PlayerGroup> {
        val results = database.create
                .select(PLAYER_GROUP.ID)
                .from(PLAYER_GROUP)
                .where(PLAYER_GROUP.PLAYER_ID.eq(player.id))
                .fetch()
        return results.map { result -> get(result.get(PLAYER_GROUP.ID)) }
                .filterNotNull()
    }

    override fun delete(entity: PlayerGroup) {
        database.create
                .deleteFrom(PLAYER_GROUP)
                .where(PLAYER_GROUP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}
