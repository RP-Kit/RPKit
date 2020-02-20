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

package com.rpkit.statbuilds.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.database.jooq.rpkit.Tables.RPKIT_CHARACTER_STAT_POINTS
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttribute
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeProvider
import com.rpkit.statbuilds.bukkit.statbuild.RPKCharacterStatPoints
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType

class RPKCharacterStatPointsTable(database: Database, private val plugin: RPKStatBuildsBukkit): Table<RPKCharacterStatPoints>(database, RPKCharacterStatPoints::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_stat_points.id.enabled")) {
        database.cacheManager.createCache("rpk-stat-builds-bukkit.rpkit_character_stat_points.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterStatPoints::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_stat_points.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create.createTableIfNotExists(RPKIT_CHARACTER_STAT_POINTS)
                .column(RPKIT_CHARACTER_STAT_POINTS.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE, SQLDataType.VARCHAR(256))
                .column(RPKIT_CHARACTER_STAT_POINTS.POINTS, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_character_stat_points").primaryKey(RPKIT_CHARACTER_STAT_POINTS.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.8.0")
        }
    }

    override fun insert(entity: RPKCharacterStatPoints): Int {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_STAT_POINTS,
                        RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID,
                        RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE,
                        RPKIT_CHARACTER_STAT_POINTS.POINTS
                )
                .values(
                        entity.character.id,
                        entity.statAttribute.name,
                        entity.points
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKCharacterStatPoints) {
        database.create
                .update(RPKIT_CHARACTER_STAT_POINTS)
                .set(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE, entity.statAttribute.name)
                .set(RPKIT_CHARACTER_STAT_POINTS.POINTS, entity.points)
                .where(RPKIT_CHARACTER_STAT_POINTS.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKCharacterStatPoints? {
        if (cache?.containsKey(id) == true) return cache[id]

        val result = database.create
                .select(
                        RPKIT_CHARACTER_STAT_POINTS.ID,
                        RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID,
                        RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE,
                        RPKIT_CHARACTER_STAT_POINTS.POINTS
                )
                .from(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.ID.eq(id))
                .fetchOne() ?: return null
        val characterId = result[RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID]
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(characterId)
        val statAttributeName = result[RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE]
        val statAttributeProvider = plugin.core.serviceManager.getServiceProvider(RPKStatAttributeProvider::class)
        val statAttribute = statAttributeProvider.getStatAttribute(statAttributeName)
        if (character == null || statAttribute == null) {
            database.create
                    .deleteFrom(RPKIT_CHARACTER_STAT_POINTS)
                    .where(RPKIT_CHARACTER_STAT_POINTS.ID.eq(id))
                    .execute()
            return null
        }
        val characterStatPoints = RPKCharacterStatPoints(
                id,
                character,
                statAttribute,
                result[RPKIT_CHARACTER_STAT_POINTS.POINTS]
        )
        cache?.put(id, characterStatPoints)
        return characterStatPoints
    }

    fun get(character: RPKCharacter, statAttribute: RPKStatAttribute): RPKCharacterStatPoints? {
        val result = database.create
                .select(RPKIT_CHARACTER_STAT_POINTS.ID)
                .from(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(statAttribute.name))
                .fetchOne() ?: return null
        return get(result[RPKIT_CHARACTER_STAT_POINTS.ID])
    }

    override fun delete(entity: RPKCharacterStatPoints) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}