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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.jooq.Tables.RPKIT_RACE
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceImpl
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the race table.
 */
class RPKRaceTable(private val database: Database, private val plugin: RPKCharactersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_race.id.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_race.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKRace::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_race.id.size"))).build())
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_race.name.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_race.name",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_race.name.size"))).build())
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
        cache?.put(id, entity)
        nameCache?.put(entity.name, id)
    }

    fun update(entity: RPKRace) {
        database.create
                .update(RPKIT_RACE)
                .set(RPKIT_RACE.NAME, entity.name)
                .where(RPKIT_RACE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        nameCache?.put(entity.name, entity.id)
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
                    id,
                    result.get(RPKIT_RACE.NAME)
            )
            cache?.put(id, race)
            nameCache?.put(race.name, id)
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
            val race = RPKRaceImpl(
                    result.get(RPKIT_RACE.ID),
                    name
            )
            cache?.put(race.id, race)
            nameCache?.put(name, race.id)
            return race
        }
    }

    fun delete(entity: RPKRace) {
        database.create
                .deleteFrom(RPKIT_RACE)
                .where(RPKIT_RACE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        nameCache?.remove(entity.name)
    }

}
