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

package com.rpkit.statbuilds.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.database.create
import com.rpkit.statbuilds.bukkit.database.jooq.Tables.RPKIT_CHARACTER_STAT_POINTS
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttribute
import com.rpkit.statbuilds.bukkit.statbuild.RPKCharacterStatPoints

class RPKCharacterStatPointsTable(private val database: Database, private val plugin: RPKStatBuildsBukkit) : Table {

    private data class CharacterStatAttributeCacheKey(
        val characterId: Int,
        val statAttributeName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_stat_points.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-stat-builds-bukkit.rpkit_character_stat_points.character_id",
            CharacterStatAttributeCacheKey::class.java,
            RPKCharacterStatPoints::class.java,
            plugin.config.getLong("caching.rpkit_character_stat_points.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterStatPoints) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_CHARACTER_STAT_POINTS,
                        RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID,
                        RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE,
                        RPKIT_CHARACTER_STAT_POINTS.POINTS
                )
                .values(
                        characterId.value,
                        entity.statAttribute.name.value,
                        entity.points
                )
                .execute()
    }

    fun update(entity: RPKCharacterStatPoints) {
        val characterId = entity.character.id ?: return
        val statAttributeName = entity.statAttribute.name
        database.create
                .update(RPKIT_CHARACTER_STAT_POINTS)
                .set(RPKIT_CHARACTER_STAT_POINTS.POINTS, entity.points)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(statAttributeName.value))
                .execute()
        cache?.set(CharacterStatAttributeCacheKey(characterId.value, statAttributeName.value), entity)
    }

    operator fun get(character: RPKCharacter, statAttribute: RPKStatAttribute): RPKCharacterStatPoints? {
        val characterId = character.id ?: return null
        val cacheKey = CharacterStatAttributeCacheKey(characterId.value, statAttribute.name.value)
        if (cache?.containsKey(cacheKey) == true) return cache[cacheKey]
        val result = database.create
            .select(RPKIT_CHARACTER_STAT_POINTS.POINTS)
            .from(RPKIT_CHARACTER_STAT_POINTS)
            .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
            .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(statAttribute.name.value))
            .fetchOne() ?: return null
        val characterStatPoints = RPKCharacterStatPoints(
            character,
            statAttribute,
            result[RPKIT_CHARACTER_STAT_POINTS.POINTS]
        )
        cache?.set(cacheKey, characterStatPoints)
        return characterStatPoints
    }

    fun delete(entity: RPKCharacterStatPoints) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(entity.statAttribute.name.value))
                .execute()
        cache?.remove(CharacterStatAttributeCacheKey(characterId.value, entity.statAttribute.name.value))
    }

}