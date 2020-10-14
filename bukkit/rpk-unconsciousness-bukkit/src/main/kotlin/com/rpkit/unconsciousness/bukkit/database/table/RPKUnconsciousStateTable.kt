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

package com.rpkit.unconsciousness.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.database.jooq.Tables.RPKIT_UNCONSCIOUS_STATE
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousState
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKUnconsciousStateTable(private val database: Database, private val plugin: RPKUnconsciousnessBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_unconscious_state.character_id.enabled")) {
        database.cacheManager.createCache("rpk-unconsciousness-bukkit.rpkit_unconscious_state.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType,
                        RPKUnconsciousState::class.java, ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_unconscious_state.character_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKUnconsciousState) {
        database.create
                .insertInto(
                        RPKIT_UNCONSCIOUS_STATE,
                        RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID,
                        RPKIT_UNCONSCIOUS_STATE.DEATH_TIME
                )
                .values(
                        entity.character.id,
                        entity.deathTime
                )
                .execute()
        cache?.put(entity.character.id, entity)
    }

    fun update(entity: RPKUnconsciousState) {
        database.create
                .update(RPKIT_UNCONSCIOUS_STATE)
                .set(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_UNCONSCIOUS_STATE.DEATH_TIME, entity.deathTime)
                .execute()
        cache?.put(entity.character.id, entity)
    }

    operator fun get(character: RPKCharacter): RPKUnconsciousState? {
        if (cache?.containsKey(character.id) == true) {
            return cache.get(character.id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID,
                            RPKIT_UNCONSCIOUS_STATE.DEATH_TIME
                    )
                    .from(RPKIT_UNCONSCIOUS_STATE)
                    .where(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID.eq(character.id))
                    .fetchOne() ?: return null
            val deathTime = result[RPKIT_UNCONSCIOUS_STATE.DEATH_TIME]
            val unconsciousState = RPKUnconsciousState(
                    character,
                    deathTime
            )
            cache?.put(character.id, unconsciousState)
            return unconsciousState
        }
    }

    fun delete(entity: RPKUnconsciousState) {
        database.create
                .deleteFrom(RPKIT_UNCONSCIOUS_STATE)
                .where(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID.eq(entity.character.id))
                .execute()
        cache?.remove(entity.character.id)
    }

}