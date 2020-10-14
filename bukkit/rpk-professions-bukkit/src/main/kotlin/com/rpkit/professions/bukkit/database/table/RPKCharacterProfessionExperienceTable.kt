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

package com.rpkit.professions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.jooq.Tables.RPKIT_CHARACTER_PROFESSION_EXPERIENCE
import com.rpkit.professions.bukkit.profession.RPKCharacterProfessionExperience
import com.rpkit.professions.bukkit.profession.RPKProfession
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKCharacterProfessionExperienceTable(
        private val database: Database,
        val plugin: RPKProfessionsBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_profession_experience.character_id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_character_profession_experience.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(
                                plugin.config.getLong("caching.rpkit_character_profession_experience.character_id.size")
                        )
                ).build()
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterProfessionExperience) {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE
                )
                .values(
                        entity.character.id,
                        entity.profession.name,
                        entity.experience
                )
                .execute()
        if (cache != null) {
            val professionExperienceMap = cache[entity.character.id] as? MutableMap<String, RPKCharacterProfessionExperience>
                    ?: mutableMapOf()
            professionExperienceMap[entity.profession.name] = entity
            cache.put(entity.character.id, professionExperienceMap)
        }
    }

    fun update(entity: RPKCharacterProfessionExperience) {
        database.create
                .update(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .set(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION, entity.profession.name)
                .set(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID.eq(entity.character.id))
                .execute()
        if (cache != null) {
            val professionExperienceMap = cache[entity.character.id] as? MutableMap<String, RPKCharacterProfessionExperience>
                    ?: mutableMapOf()
            professionExperienceMap[entity.profession.name] = entity
            cache.put(entity.character.id, professionExperienceMap)
        }
    }

    operator fun get(character: RPKCharacter, profession: RPKProfession): RPKCharacterProfessionExperience? {
        val result = database.create
                .select(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE)
                .from(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION.eq(profession.name))
                .fetchOne() ?: return null
        val characterProfessionExperience = RPKCharacterProfessionExperience(
                character,
                profession,
                result[RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE]
        )
        if (cache != null) {
            val professionExperienceMap = cache[character.id] as? MutableMap<String, RPKCharacterProfessionExperience>
                    ?: mutableMapOf()
            professionExperienceMap[profession.name] = characterProfessionExperience
            cache.put(character.id, professionExperienceMap)
        }
        return characterProfessionExperience
    }

    fun delete(entity: RPKCharacterProfessionExperience) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION.eq(entity.profession.name))
                .execute()
        if (cache != null) {
            val professionExperienceMap = cache[entity.character.id] as? MutableMap<String, RPKCharacterProfessionExperience>
                    ?: mutableMapOf()
            professionExperienceMap.remove(entity.profession.name)
            cache.put(entity.character.id, professionExperienceMap)
        }
    }

    fun delete(character: RPKCharacter) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .execute()
        cache?.remove(character.id)
    }

}