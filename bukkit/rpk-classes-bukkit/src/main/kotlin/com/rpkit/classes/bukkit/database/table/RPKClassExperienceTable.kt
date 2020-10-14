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

package com.rpkit.classes.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.classes.bukkit.classes.RPKClassExperience
import com.rpkit.classes.bukkit.database.jooq.Tables.RPKIT_CLASS_EXPERIENCE
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKClassExperienceTable(private val database: Database, private val plugin: RPKClassesBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_class_experience.character_id.enabled")) {
        database.cacheManager.createCache("rpk-classes-bukkit.rpkit_class_experience.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_class_experience.character_id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKClassExperience) {
        database.create
                .insertInto(
                        RPKIT_CLASS_EXPERIENCE,
                        RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                        RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .values(
                        entity.character.id,
                        entity.`class`.name,
                        entity.experience
                )
                .execute()
        val classMap = cache?.get(entity.character.id) as? MutableMap<String, RPKClassExperience> ?: mutableMapOf()
        classMap[entity.`class`.name] = entity
        cache?.put(entity.character.id, classMap)
    }

    fun update(entity: RPKClassExperience) {
        database.create
                .update(RPKIT_CLASS_EXPERIENCE)
                .set(RPKIT_CLASS_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(entity.`class`.name))
                .execute()
        val classMap = cache?.get(entity.character.id) as? MutableMap<String, RPKClassExperience> ?: mutableMapOf()
        classMap[entity.`class`.name] = entity
        cache?.put(entity.character.id, classMap)
    }

    operator fun get(character: RPKCharacter, `class`: RPKClass): RPKClassExperience? {
        if (cache?.containsKey(character.id) == true) {
            if (cache[character.id].containsKey(`class`.name)) {
                return cache[character.id][`class`.name] as RPKClassExperience
            }
        }
        val result = database.create
                .select(
                        RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                        RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .from(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(`class`.name))
                .fetchOne() ?: return null
        val classExperience = RPKClassExperience(
                character,
                `class`,
                result.get(RPKIT_CLASS_EXPERIENCE.EXPERIENCE)
        )
        val classMap = cache?.get(classExperience.character.id) as? MutableMap<String, RPKClassExperience> ?: mutableMapOf()
        classMap[classExperience.`class`.name] = classExperience
        cache?.put(classExperience.character.id, classMap)
        return classExperience
    }

    fun delete(entity: RPKClassExperience) {
        database.create
                .deleteFrom(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(entity.`class`.name))
                .execute()
        val classMap = cache?.get(entity.character.id) as? MutableMap<String, RPKClassExperience> ?: mutableMapOf()
        classMap.remove(entity.`class`.name)
        cache?.put(entity.character.id, classMap)
    }

}