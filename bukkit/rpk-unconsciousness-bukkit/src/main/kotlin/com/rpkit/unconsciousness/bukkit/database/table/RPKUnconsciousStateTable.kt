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

package com.rpkit.unconsciousness.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.database.create
import com.rpkit.unconsciousness.bukkit.database.jooq.Tables.RPKIT_UNCONSCIOUS_STATE
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousState


class RPKUnconsciousStateTable(private val database: Database, private val plugin: RPKUnconsciousnessBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_unconscious_state.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-unconsciousness-bukkit.rpkit_unconscious_state.character_id",
            Int::class.javaObjectType,
            RPKUnconsciousState::class.java,
            plugin.config.getLong("caching.rpkit_unconscious_state.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKUnconsciousState) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_UNCONSCIOUS_STATE,
                        RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID,
                        RPKIT_UNCONSCIOUS_STATE.DEATH_TIME
                )
                .values(
                    characterId.value,
                    entity.deathTime
                )
                .execute()
        cache?.set(characterId.value, entity)
    }

    fun update(entity: RPKUnconsciousState) {
        val characterId = entity.character.id ?: return
        database.create
                .update(RPKIT_UNCONSCIOUS_STATE)
                .set(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID, characterId.value)
                .set(RPKIT_UNCONSCIOUS_STATE.DEATH_TIME, entity.deathTime)
                .execute()
        cache?.set(characterId.value, entity)
    }

    operator fun get(character: RPKCharacter): RPKUnconsciousState? {
        val characterId = character.id ?: return null
        if (cache?.containsKey(characterId.value) == true) {
            return cache[characterId.value]
        } else {
            val result = database.create
                    .select(
                            RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID,
                            RPKIT_UNCONSCIOUS_STATE.DEATH_TIME
                    )
                    .from(RPKIT_UNCONSCIOUS_STATE)
                    .where(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID.eq(characterId.value))
                    .fetchOne() ?: return null
            val deathTime = result[RPKIT_UNCONSCIOUS_STATE.DEATH_TIME]
            val unconsciousState = RPKUnconsciousState(
                    character,
                    deathTime
            )
            cache?.set(characterId.value, unconsciousState)
            return unconsciousState
        }
    }

    fun delete(entity: RPKUnconsciousState) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_UNCONSCIOUS_STATE)
                .where(RPKIT_UNCONSCIOUS_STATE.CHARACTER_ID.eq(characterId.value))
                .execute()
        cache?.remove(characterId.value)
    }

}