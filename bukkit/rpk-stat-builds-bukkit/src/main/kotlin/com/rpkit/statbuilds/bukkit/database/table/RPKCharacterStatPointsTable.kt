/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.database.create
import com.rpkit.statbuilds.bukkit.database.jooq.Tables.RPKIT_CHARACTER_STAT_POINTS
import com.rpkit.statbuilds.bukkit.database.jooq.tables.records.RpkitCharacterStatPointsRecord
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttribute
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeName
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeService
import com.rpkit.statbuilds.bukkit.statbuild.RPKCharacterStatPoints
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

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

    fun insert(entity: RPKCharacterStatPoints): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
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
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert character stat points", exception)
            throw exception
        }
    }

    fun update(entity: RPKCharacterStatPoints): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val statAttributeName = entity.statAttribute.name
            database.create
                .update(RPKIT_CHARACTER_STAT_POINTS)
                .set(RPKIT_CHARACTER_STAT_POINTS.POINTS, entity.points)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(statAttributeName.value))
                .execute()
            cache?.set(CharacterStatAttributeCacheKey(characterId.value, statAttributeName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update character stat points", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter, statAttribute: RPKStatAttribute): CompletableFuture<RPKCharacterStatPoints?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val cacheKey = CharacterStatAttributeCacheKey(characterId.value, statAttribute.name.value)
        if (cache?.containsKey(cacheKey) == true) return CompletableFuture.completedFuture(cache[cacheKey])
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_CHARACTER_STAT_POINTS.POINTS)
                .from(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(statAttribute.name.value))
                .fetchOne() ?: return@supplyAsync null
            val characterStatPoints = RPKCharacterStatPoints(
                character,
                statAttribute,
                result[RPKIT_CHARACTER_STAT_POINTS.POINTS]
            )
            cache?.set(cacheKey, characterStatPoints)
            return@supplyAsync characterStatPoints
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character stat points", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter): CompletableFuture<List<RPKCharacterStatPoints>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.supplyAsync {
            database.create
                .selectFrom(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
                .fetch()
                .map { it.toDomain() }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character stat points", exception)
            throw exception
        }
    }

    fun delete(entity: RPKCharacterStatPoints): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHARACTER_STAT_POINTS)
                .where(RPKIT_CHARACTER_STAT_POINTS.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_STAT_POINTS.STAT_ATTRIBUTE.eq(entity.statAttribute.name.value))
                .execute()
            cache?.remove(CharacterStatAttributeCacheKey(characterId.value, entity.statAttribute.name.value))
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete character stat points", exception)
            throw exception
        }
    }

    private fun RpkitCharacterStatPointsRecord.toDomain(): RPKCharacterStatPoints? {
        val character = Services[RPKCharacterService::class.java]?.getCharacter(RPKCharacterId(characterId))?.join() ?: return null
        val statAttribute = Services[RPKStatAttributeService::class.java]?.getStatAttribute(
            RPKStatAttributeName(statAttribute)
        ) ?: return null
        return RPKCharacterStatPoints(
            character,
            statAttribute,
            points
        )
    }

}