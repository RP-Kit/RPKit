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
import com.rpkit.chat.bukkit.database.jooq.rpkit.Tables.RPKIT_SNOOPER
import com.rpkit.chat.bukkit.snooper.RPKSnooper
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the snooper table.
 */
class RPKSnooperTable(database: Database, private val plugin: RPKChatBukkit): Table<RPKSnooper>(database, RPKSnooper::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKSnooper::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "0.3.0") {
            database.create
                    .truncate(RPKIT_SNOOPER)
                    .execute()
            database.create
                    .alterTable(RPKIT_SNOOPER)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_SNOOPER)
                    .addColumn(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_SNOOPER)
                .column(RPKIT_SNOOPER.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_snooper").primaryKey(RPKIT_SNOOPER.ID)
                )
                .execute()
    }

    override fun insert(entity: RPKSnooper): Int {
        database.create
                .insertInto(
                        RPKIT_SNOOPER,
                        RPKIT_SNOOPER.MINECRAFT_PROFILE_ID
                )
                .values(entity.minecraftProfile.id)
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKSnooper) {
        database.create
                .update(RPKIT_SNOOPER)
                .set(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .where(RPKIT_SNOOPER.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKSnooper? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)
                    .from(RPKIT_SNOOPER)
                    .where(RPKIT_SNOOPER.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfileId = result.get(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
            if (minecraftProfile != null) {
                val snooper = RPKSnooper(
                        id,
                        minecraftProfile
                )
                cache.put(id, snooper)
                return snooper
            } else {
                database.create
                        .deleteFrom(RPKIT_SNOOPER)
                        .where(RPKIT_SNOOPER.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets the snooper instance for a Minecraft profile.
     * If the player does not have a snooper entry, null is returned.
     *
     * @param minecraftProfile The player
     * @return The snooper instance, or null if none exists
     */
    fun get(minecraftProfile: RPKMinecraftProfile): RPKSnooper? {
        val result = database.create
                .select(RPKIT_SNOOPER.ID)
                .from(RPKIT_SNOOPER)
                .where(RPKIT_SNOOPER.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_SNOOPER.ID))
    }

    /**
     * Gets all snoopers
     *
     * @return A list containing all snoopers
     */
    fun getAll(): List<RPKSnooper> {
        val results = database.create
                .select(RPKIT_SNOOPER.ID)
                .from(RPKIT_SNOOPER)
                .fetch()
        val snoopers = results.map { result ->
            get(result.get(RPKIT_SNOOPER.ID))
        }.filterNotNull()
        return snoopers
    }

    override fun delete(entity: RPKSnooper) {
        database.create
                .deleteFrom(RPKIT_SNOOPER)
                .where(RPKIT_SNOOPER.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}