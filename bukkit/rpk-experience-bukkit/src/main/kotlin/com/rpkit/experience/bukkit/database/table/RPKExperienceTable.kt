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

package com.rpkit.experience.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.database.create
import com.rpkit.experience.bukkit.database.jooq.Tables.RPKIT_EXPERIENCE_
import com.rpkit.experience.bukkit.experience.RPKExperienceValue


class RPKExperienceTable(private val database: Database, private val plugin: RPKExperienceBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_experience.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-experience-bukkit.rpkit_experience.character_id",
            Int::class.javaObjectType,
            RPKExperienceValue::class.java,
            plugin.config.getLong("caching.rpkit_experience.character_id.size")
        )
    } else {
        null
    }

    fun delete(entity: RPKExperienceValue) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_EXPERIENCE_)
                .where(RPKIT_EXPERIENCE_.CHARACTER_ID.eq(entity.character.id))
                .execute()
        cache?.remove(characterId)
    }

    operator fun get(character: RPKCharacter): RPKExperienceValue? {
        val characterId = character.id ?: return null
        if (cache?.containsKey(characterId) == true) {
            return cache[characterId]
        } else {
            val result = database.create
                    .select(
                            RPKIT_EXPERIENCE_.CHARACTER_ID,
                            RPKIT_EXPERIENCE_.VALUE
                    )
                    .from(RPKIT_EXPERIENCE_)
                    .where(RPKIT_EXPERIENCE_.CHARACTER_ID.eq(characterId))
                    .fetchOne() ?: return null
            val experienceValue = RPKExperienceValue(
                    character,
                    result.get(RPKIT_EXPERIENCE_.VALUE)
            )
            cache?.set(characterId, experienceValue)
            return experienceValue
        }
    }

    fun insert(entity: RPKExperienceValue) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_EXPERIENCE_,
                        RPKIT_EXPERIENCE_.CHARACTER_ID,
                        RPKIT_EXPERIENCE_.VALUE
                )
                .values(
                        entity.character.id,
                        entity.value
                )
                .execute()
        cache?.set(characterId, entity)
    }

    fun update(entity: RPKExperienceValue) {
        val characterId = entity.character.id ?: return
        database.create
                .update(RPKIT_EXPERIENCE_)
                .set(RPKIT_EXPERIENCE_.VALUE, entity.value)
                .where(RPKIT_EXPERIENCE_.CHARACTER_ID.eq(entity.character.id))
                .execute()
        cache?.set(characterId, entity)
    }

}