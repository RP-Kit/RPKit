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

package com.rpkit.unconsciousness.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.database.jooq.rpkit.Tables.RPKIT_UNCONSCIOUS_STATE
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousState
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import java.sql.Timestamp


class RPKUnconsciousStateTable(database: Database, private val plugin: RPKUnconsciousnessBukkit): Table<RPKUnconsciousState>(database, RPKUnconsciousState::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_unconscious_state.id.enabled")) {
        database.cacheManager.createCache("rpk-unconsciousness-bukkit.rpkit_unconscious_state.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType,
                        RPKUnconsciousState::class.java, ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_unconscious_state.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_UNCONSCIOUS_STATE)
                .column(RPKIT_UNCONSCIOUS_STATE.ID,
                        if (database.dialect == SQLDialect.SQLITE)
                            SQLiteDataType.INTEGER.identity(true)
                        else
                            SQLDataType.INTEGER.identity(true))
                .column(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_UNCONSCIOUS_STATE.DEATH_TIME, SQLDataType.TIMESTAMP)
                .constraints(
                        constraint("pk_rpkit_unconscious_state").primaryKey(RPKIT_UNCONSCIOUS_STATE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.5.0")
        }
    }

    override fun insert(entity: RPKUnconsciousState): Int {
        database.create
                .insertInto(
                        RPKIT_UNCONSCIOUS_STATE,
                        RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID,
                        RPKIT_UNCONSCIOUS_STATE.DEATH_TIME
                )
                .values(
                        entity.character.id,
                        Timestamp(entity.deathTime)
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKUnconsciousState) {
        database.create
                .update(RPKIT_UNCONSCIOUS_STATE)
                .set(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_UNCONSCIOUS_STATE.DEATH_TIME, Timestamp(entity.deathTime))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKUnconsciousState? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID,
                            RPKIT_UNCONSCIOUS_STATE.DEATH_TIME
                    )
                    .from(RPKIT_UNCONSCIOUS_STATE)
                    .where(RPKIT_UNCONSCIOUS_STATE.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val character = characterProvider.getCharacter(result[RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID])
            val deathTime = result[RPKIT_UNCONSCIOUS_STATE.DEATH_TIME]
            if (character != null) {
                val unconsciousState = RPKUnconsciousState(
                        id,
                        character,
                        deathTime.time
                )
                cache?.put(id, unconsciousState)
                return unconsciousState
            } else {
                database.create
                        .deleteFrom(RPKIT_UNCONSCIOUS_STATE)
                        .where(RPKIT_UNCONSCIOUS_STATE.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
        }
    }

    fun get(character: RPKCharacter): RPKUnconsciousState? {
        val result = database.create
                .select(RPKIT_UNCONSCIOUS_STATE.ID)
                .from(RPKIT_UNCONSCIOUS_STATE)
                .where(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_UNCONSCIOUS_STATE.ID])
    }

    override fun delete(entity: RPKUnconsciousState) {
        database.create
                .deleteFrom(RPKIT_UNCONSCIOUS_STATE)
                .where(RPKIT_UNCONSCIOUS_STATE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}