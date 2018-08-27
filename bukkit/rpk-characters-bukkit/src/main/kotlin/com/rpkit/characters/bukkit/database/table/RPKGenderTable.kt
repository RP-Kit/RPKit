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

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.jooq.rpkit.Tables.RPKIT_GENDER
import com.rpkit.characters.bukkit.gender.RPKGender
import com.rpkit.characters.bukkit.gender.RPKGenderImpl
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType

/**
 * Represents the gender table.
 */
class RPKGenderTable(database: Database, private val plugin: RPKCharactersBukkit): Table<RPKGender>(database, RPKGender::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_gender.id.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_gender.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKGender::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_gender.id.size"))).build())
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_gender.name.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_gender.name",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_gender.name.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create.createTableIfNotExists(RPKIT_GENDER)
                .column(RPKIT_GENDER.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_GENDER.NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_gender").primaryKey(RPKIT_GENDER.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(entity: RPKGender): Int {
        database.create
                .insertInto(
                        RPKIT_GENDER,
                        RPKIT_GENDER.NAME
                )
                .values(
                        entity.name
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        nameCache?.put(entity.name, id)
        return id
    }

    override fun update(entity: RPKGender) {
        database.create
                .update(RPKIT_GENDER)
                .set(RPKIT_GENDER.NAME, entity.name)
                .where(RPKIT_GENDER.ID.eq(entity.id))
                .execute()
    }

    override fun get(id: Int): RPKGender? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_GENDER.ID,
                            RPKIT_GENDER.NAME
                    )
                    .from(RPKIT_GENDER)
                    .where(RPKIT_GENDER.ID.eq(id))
                    .fetchOne() ?: return null
            val gender = RPKGenderImpl(
                    result.get(RPKIT_GENDER.ID),
                    result.get(RPKIT_GENDER.NAME)
            )
            cache?.put(id, gender)
            nameCache?.put(gender.name, id)
            return gender
        }
    }

    fun getAll(): List<RPKGender> {
        val results = database.create
                .select(RPKIT_GENDER.ID)
                .from(RPKIT_GENDER)
                .fetch()
        return results.map { result -> get(result.get(RPKIT_GENDER.ID)) }
                .filterNotNull()
    }

    /**
     * Gets a gender by name.
     * If no gender is found with the given name, null is returned.
     *
     * @param name The name of the gender
     * @return The gender, or null if no gender is found with the given name
     */
    operator fun get(name: String): RPKGender? {
        if (nameCache?.containsKey(name) == true) {
            return get(nameCache.get(name) as Int)
        } else {
            val result = database.create
                    .select(
                            RPKIT_GENDER.ID,
                            RPKIT_GENDER.NAME
                    )
                    .from(RPKIT_GENDER)
                    .where(RPKIT_GENDER.NAME.eq(name))
                    .fetchOne() ?: return null
            val gender = RPKGenderImpl(
                    result.get(RPKIT_GENDER.ID),
                    result.get(RPKIT_GENDER.NAME)
            )
            cache?.put(gender.id, gender)
            nameCache?.put(name, gender.id)
            return gender
        }
    }

    override fun delete(entity: RPKGender) {
        database.create
                .deleteFrom(RPKIT_GENDER)
                .where(RPKIT_GENDER.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        nameCache?.remove(entity.name)
    }

}
