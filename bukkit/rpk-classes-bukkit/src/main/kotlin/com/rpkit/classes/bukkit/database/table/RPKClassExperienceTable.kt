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

package com.rpkit.classes.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.classes.bukkit.classes.RPKClassExperience
import com.rpkit.classes.bukkit.database.create
import com.rpkit.classes.bukkit.database.jooq.Tables.RPKIT_CLASS_EXPERIENCE
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table


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

    fun insert(entity: RPKClassExperience) {
        val characterId = entity.character.id ?: return
        val className = entity.`class`.name
        database.create
                .insertInto(
                        RPKIT_CLASS_EXPERIENCE,
                        RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                        RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .values(
                        characterId.value,
                        entity.`class`.name,
                        entity.experience
                )
                .execute()
        cache?.set(CharacterClassCacheKey(characterId.value, className), entity)
    }

    fun update(entity: RPKClassExperience) {
        val characterId = entity.character.id ?: return
        val className = entity.`class`.name
        database.create
                .update(RPKIT_CLASS_EXPERIENCE)
                .set(RPKIT_CLASS_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(entity.`class`.name))
                .execute()
        cache?.set(CharacterClassCacheKey(characterId.value, className), entity)
    }

    operator fun get(character: RPKCharacter, `class`: RPKClass): RPKClassExperience? {
        val characterId = character.id ?: return null
        val className = `class`.name
        val cacheKey = CharacterClassCacheKey(characterId.value, className)
        if (cache?.containsKey(cacheKey) == true) {
            return cache[cacheKey]
        }
        val result = database.create
                .select(
                        RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                        RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .from(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(`class`.name))
                .fetchOne() ?: return null
        val classExperience = RPKClassExperience(
                character,
                `class`,
                result.get(RPKIT_CLASS_EXPERIENCE.EXPERIENCE)
        )
        cache?.set(cacheKey, classExperience)
        return classExperience
    }

    fun delete(entity: RPKClassExperience) {
        val characterId = entity.character.id ?: return
        val className = entity.`class`.name
        database.create
                .deleteFrom(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(className))
                .execute()
        cache?.remove(CharacterClassCacheKey(characterId.value, className))
    }

}