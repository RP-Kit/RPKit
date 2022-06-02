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

package com.rpkit.classes.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.classes.bukkit.classes.RPKClassExperience
import com.rpkit.classes.bukkit.database.create
import com.rpkit.classes.bukkit.database.jooq.Tables.RPKIT_CLASS_EXPERIENCE
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKClassExperienceTable(private val database: Database, private val plugin: RPKClassesBukkit) : Table {

    private data class CharacterClassCacheKey(
        val characterId: Int,
        val className: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_class_experience.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-classes-bukkit.rpkit_class_experience.character_id",
                CharacterClassCacheKey::class.java,
            RPKClassExperience::class.java,
            plugin.config.getLong("caching.rpkit_class_experience.character_id.size"))
    } else {
        null
    }

    fun insert(entity: RPKClassExperience): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val className = entity.`class`.name
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CLASS_EXPERIENCE,
                    RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                    RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                    RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .values(
                    characterId.value,
                    entity.`class`.name.value,
                    entity.experience
                )
                .execute()
            cache?.set(CharacterClassCacheKey(characterId.value, className.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert class experience", exception)
            throw exception
        }
    }

    fun update(entity: RPKClassExperience): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val className = entity.`class`.name
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_CLASS_EXPERIENCE)
                .set(RPKIT_CLASS_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(entity.`class`.name.value))
                .execute()
            cache?.set(CharacterClassCacheKey(characterId.value, className.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update class experience", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter, `class`: RPKClass): CompletableFuture<RPKClassExperience?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val className = `class`.name
        val cacheKey = CharacterClassCacheKey(characterId.value, className.value)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                    RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                    RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .from(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(`class`.name.value))
                .fetchOne() ?: return@supplyAsync null
            val classExperience = RPKClassExperience(
                character,
                `class`,
                result.get(RPKIT_CLASS_EXPERIENCE.EXPERIENCE)
            )
            cache?.set(cacheKey, classExperience)
            return@supplyAsync classExperience
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get class experience", exception)
            throw exception
        }
    }

    fun delete(entity: RPKClassExperience): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val className = entity.`class`.name
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(className.value))
                .execute()
            cache?.remove(CharacterClassCacheKey(characterId.value, className.value))
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete class experience", exception)
            throw exception
        }
    }

}