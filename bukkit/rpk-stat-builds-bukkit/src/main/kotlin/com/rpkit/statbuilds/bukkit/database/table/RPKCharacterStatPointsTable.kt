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
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.database.jooq.Tables.RPKIT_CHARACTER_STAT_POINTS
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttribute
import com.rpkit.statbuilds.bukkit.statbuild.RPKCharacterStatPoints
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class RPKCharacterStatPointsTable(private val database: Database, private val plugin: RPKStatBuildsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_stat_points.character_id.enabled")) {
        database.cacheManager.createCache(
                "rpk-stat-builds-bukkit.rpkit_character_stat_points.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String::class.java,
                        RPKCharacterStatPoints::class.java,
                        ResourcePoolsBuilder.heap(
                                plugin.config.getLong("caching.rpkit_character_stat_points.character_id.size")
                        )
                )
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterStatPoints) {
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
    }

    fun update(entity: RPKCharacterStatPoints) {
        database.create
                .update(RPKIT_CHARACTER_STAT_POINTS)
                .set(RPKIT_CHARACTER_STAT_POINTS.POINTS, entity.points)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(entity.statAttribute.name))
                .execute()
        cache?.put("${entity.character.id},${entity.statAttribute.name}", entity)
    }

    operator fun get(character: RPKCharacter, statAttribute: RPKStatAttribute): RPKCharacterStatPoints? {
        val cacheKey = cacheKey(character, statAttribute)
        if (cache?.containsKey(cacheKey) == true) return cache[cacheKey]
        val result = database.create
                .select(RPKIT_CHARACTER_STAT_POINTS.POINTS)
                .from(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(statAttribute.name))
                .fetchOne() ?: return null
        val characterStatPoints = RPKCharacterStatPoints(
                character,
                statAttribute,
                result[RPKIT_CHARACTER_STAT_POINTS.POINTS]
        )
        cache?.put(cacheKey, characterStatPoints)
        return characterStatPoints
    }

    fun delete(entity: RPKCharacterStatPoints) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(entity.statAttribute.name))
                .execute()
        cache?.remove(cacheKey(entity.character, entity.statAttribute))
    }

    private fun cacheKey(character: RPKCharacter, statAttribute: RPKStatAttribute) = "${character.id},${statAttribute.name}"

}