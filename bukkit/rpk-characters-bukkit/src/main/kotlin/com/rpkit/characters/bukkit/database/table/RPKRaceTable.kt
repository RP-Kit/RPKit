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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.create
import com.rpkit.characters.bukkit.database.jooq.Tables.RPKIT_RACE
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceId
import com.rpkit.characters.bukkit.race.RPKRaceImpl
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table

/**
 * Represents the race table.
 */
class RPKRaceTable(private val database: Database, private val plugin: RPKCharactersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_race.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-characters-bukkit.rpkit_race.id",
            Int::class.javaObjectType,
            RPKRace::class.java,
            plugin.config.getLong("caching.rpkit_race.id.size")
        )
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_race.name.enabled")) {
        database.cacheManager.createCache(
            "rpk-characters-bukkit.rpkit_race.name",
            String::class.java,
            Int::class.javaObjectType,
            plugin.config.getLong("caching.rpkit_race.name.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKRace) {
        database.create
                .insertInto(
                        RPKIT_RACE,
                        RPKIT_RACE.NAME
                )
                .values(
                        entity.name
                )
                .execute()
        val id = database.create.lastID().toInt()
        cache?.set(id, entity)
        nameCache?.set(entity.name, id)
    }

    fun update(entity: RPKRace) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_RACE)
                .set(RPKIT_RACE.NAME, entity.name)
                .where(RPKIT_RACE.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
        nameCache?.set(entity.name, id.value)
    }

    operator fun get(id: Int): RPKRace? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_RACE.NAME
                    )
                    .from(
                            RPKIT_RACE
                    )
                    .where(RPKIT_RACE.ID.eq(id))
                    .fetchOne() ?: return null
            val race = RPKRaceImpl(
                    RPKRaceId(id),
                    result.get(RPKIT_RACE.NAME)
            )
            cache?.set(id, race)
            nameCache?.set(race.name, id)
            return race
        }
    }

    fun getAll(): List<RPKRace> {
        val results = database.create
                .select(RPKIT_RACE.ID)
                .from(RPKIT_RACE)
                .fetch()
        return results.map { result -> get(result.get(RPKIT_RACE.ID)) }
                .filterNotNull()
    }

    /**
     * Gets a race by name.
     * If no race is found with the given name, null is returned.
     *
     * @param name The name of the race
     * @return The race, or null if no race is found with the given name
     */
    operator fun get(name: String): RPKRace? {
        if (nameCache?.containsKey(name) == true) {
            return get(nameCache.get(name) as Int)
        } else {
            val result = database.create
                    .select(RPKIT_RACE.ID)
                    .from(RPKIT_RACE)
                    .where(RPKIT_RACE.NAME.eq(name))
                    .fetchOne() ?: return null
            val id = result[RPKIT_RACE.ID]
            val race = RPKRaceImpl(
                    RPKRaceId(id),
                    name
            )
            cache?.set(id, race)
            nameCache?.set(name, id)
            return race
        }
    }

    fun delete(entity: RPKRace) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_RACE)
                .where(RPKIT_RACE.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
        nameCache?.remove(entity.name)
    }

}
