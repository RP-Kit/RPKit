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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.database.jooq.rpkit.Tables.RPKIT_RACE
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceImpl
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the race table.
 */
class RPKRaceTable(database: Database): Table<RPKRace>(database, RPKRace::class) {

    private val cache = database.cacheManager.createCache("rpk-characters-bukkit.rpkit_race.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKRace::class.java,
                    ResourcePoolsBuilder.heap(20L)).build())
    private val nameCache = database.cacheManager.createCache("rpk-characters-bukkit.rpkit_race.name",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                    ResourcePoolsBuilder.heap(20L)).build())

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_RACE)
                .column(RPKIT_RACE.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_RACE.NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_race").primaryKey(RPKIT_RACE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(entity: RPKRace): Int {
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
        cache.put(id, entity)
        nameCache.put(entity.name, id)
        return id
    }

    override fun update(entity: RPKRace) {
        database.create
                .update(RPKIT_RACE)
                .set(RPKIT_RACE.NAME, entity.name)
                .where(RPKIT_RACE.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
        nameCache.put(entity.name, entity.id)
    }

    override fun get(id: Int): RPKRace? {
        if (cache.containsKey(id)) {
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
            cache.put(id, race)
            nameCache.put(race.name, id)
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
        if (nameCache.containsKey(name)) {
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
            cache.put(race.id, race)
            nameCache.put(name, race.id)
            return race
        }
    }

    override fun delete(entity: RPKRace) {
        database.create
                .deleteFrom(RPKIT_RACE)
                .where(RPKIT_RACE.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
        nameCache.remove(entity.name)
    }

}
