/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.moderation.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.jooq.rpkit.Tables
import com.rpkit.moderation.bukkit.database.jooq.rpkit.Tables.RPKIT_VANISH_STATE
import com.rpkit.moderation.bukkit.vanish.RPKVanishState
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKVanishStateTable(database: Database, private val plugin: RPKModerationBukkit): Table<RPKVanishState>(database, RPKVanishState::class) {

    private val cache = database.cacheManager.createCache("rpk-moderation-bukkit.rpkit_vanish_state.id", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKVanishState::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_VANISH_STATE)
                .column(RPKIT_VANISH_STATE.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_VANISH_STATE.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_VANISH_STATE.VANISHED, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_vanish_state").primaryKey(RPKIT_VANISH_STATE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.5.2")
        }
        if (database.getTableVersion(this) == "1.5.0") {
            database.create
                    .alterTable(Tables.RPKIT_VANISH_STATE)
                    .alterColumn(Tables.RPKIT_VANISH_STATE.ID)
                        .set(if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                    .execute()
            database.setTableVersion(this, "1.5.2")
        }
    }

    override fun insert(entity: RPKVanishState): Int {
        database.create
                .insertInto(
                        RPKIT_VANISH_STATE,
                        RPKIT_VANISH_STATE.MINECRAFT_PROFILE_ID,
                        RPKIT_VANISH_STATE.VANISHED
                )
                .values(
                        entity.minecraftProfile.id,
                        if (entity.isVanished) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKVanishState) {
        database.create
                .update(RPKIT_VANISH_STATE)
                .set(RPKIT_VANISH_STATE.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(RPKIT_VANISH_STATE.VANISHED, if (entity.isVanished) 1.toByte() else 0.toByte())
                .where(RPKIT_VANISH_STATE.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKVanishState? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_VANISH_STATE.MINECRAFT_PROFILE_ID,
                            RPKIT_VANISH_STATE.VANISHED
                    )
                    .from(RPKIT_VANISH_STATE)
                    .where(RPKIT_VANISH_STATE.ID.eq(id))
                    .fetchOne() ?: return null
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(result[RPKIT_VANISH_STATE.MINECRAFT_PROFILE_ID])
            if (minecraftProfile != null) {
                val vanishState = RPKVanishState(
                        id,
                        minecraftProfile,
                        result[RPKIT_VANISH_STATE.VANISHED] == 1.toByte()
                )
                cache.put(id, vanishState)
                return vanishState
            } else {
                database.create
                        .deleteFrom(RPKIT_VANISH_STATE)
                        .where(RPKIT_VANISH_STATE.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    override fun delete(entity: RPKVanishState) {
        database.create
                .deleteFrom(RPKIT_VANISH_STATE)
                .where(RPKIT_VANISH_STATE.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKVanishState? {
        val result = database.create
                .select(RPKIT_VANISH_STATE.ID)
                .from(RPKIT_VANISH_STATE)
                .where(RPKIT_VANISH_STATE.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_VANISH_STATE.ID])
    }

}