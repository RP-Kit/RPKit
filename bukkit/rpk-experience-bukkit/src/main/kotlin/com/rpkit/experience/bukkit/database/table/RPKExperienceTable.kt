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
import com.rpkit.experience.bukkit.database.jooq.Tables.RPKIT_EXPERIENCE_
import com.rpkit.experience.bukkit.experience.RPKExperienceValue
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKExperienceTable(private val database: Database, private val plugin: RPKExperienceBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_experience.character_id.enabled")) {
        database.cacheManager.createCache("rpk-experience-bukkit.rpkit_experience.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKExperienceValue::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_experience.character_id.size"))).build())
    } else {
        null
    }

    fun delete(entity: RPKExperienceValue) {
        database.create
                .deleteFrom(RPKIT_EXPERIENCE_)
                .where(RPKIT_EXPERIENCE_.CHARACTER_ID.eq(entity.character.id))
                .execute()
        cache?.remove(entity.character.id)
    }

    operator fun get(character: RPKCharacter): RPKExperienceValue? {
        if (cache?.containsKey(character.id) == true) {
            return cache.get(character.id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_EXPERIENCE_.CHARACTER_ID,
                            RPKIT_EXPERIENCE_.VALUE
                    )
                    .from(RPKIT_EXPERIENCE_)
                    .where(RPKIT_EXPERIENCE_.CHARACTER_ID.eq(character.id))
                    .fetchOne() ?: return null
            val experienceValue = RPKExperienceValue(
                    character,
                    result.get(RPKIT_EXPERIENCE_.VALUE)
            )
            cache?.put(character.id, experienceValue)
            return experienceValue
        }
    }

    fun insert(entity: RPKExperienceValue) {
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
        cache?.put(entity.character.id, entity)
    }

    fun update(entity: RPKExperienceValue) {
        database.create
                .update(RPKIT_EXPERIENCE_)
                .set(RPKIT_EXPERIENCE_.VALUE, entity.value)
                .where(RPKIT_EXPERIENCE_.CHARACTER_ID.eq(entity.character.id))
                .execute()
        cache?.put(entity.character.id, entity)
    }

}