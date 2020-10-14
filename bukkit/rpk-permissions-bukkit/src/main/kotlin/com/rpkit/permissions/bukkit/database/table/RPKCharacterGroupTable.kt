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

package com.rpkit.permissions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.jooq.Tables.RPKIT_CHARACTER_GROUP
import com.rpkit.permissions.bukkit.group.RPKCharacterGroup
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupService
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKCharacterGroupTable(private val database: Database, private val plugin: RPKPermissionsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_group.character_id.enabled")) {
        database.cacheManager.createCache("rpk-permissions-bukkit.rpkit_character_group.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_group.character_id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKCharacterGroup) {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_GROUP,
                        RPKIT_CHARACTER_GROUP.CHARACTER_ID,
                        RPKIT_CHARACTER_GROUP.GROUP_NAME,
                        RPKIT_CHARACTER_GROUP.PRIORITY
                )
                .values(
                        entity.character.id,
                        entity.group.name,
                        entity.priority
                )
                .execute()
        val groupMap = cache?.get(entity.character.id) as? MutableMap<String, RPKCharacterGroup> ?: mutableMapOf()
        groupMap[entity.group.name] = entity
        cache?.put(entity.character.id, groupMap)
    }

    fun update(entity: RPKCharacterGroup) {
        database.create
                .update(RPKIT_CHARACTER_GROUP)
                .set(RPKIT_CHARACTER_GROUP.PRIORITY, entity.priority)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(entity.group.name))
                .execute()
        val groupMap = cache?.get(entity.character.id) as? MutableMap<String, RPKCharacterGroup> ?: mutableMapOf()
        groupMap[entity.group.name] = entity
        cache?.put(entity.character.id, groupMap)
    }

    operator fun get(character: RPKCharacter, group: RPKGroup): RPKCharacterGroup? {
        if (cache?.containsKey(character.id) == true) {
            val groupMap = cache[character.id] as? MutableMap<String, RPKCharacterGroup> ?: mutableMapOf()
            if (groupMap.contains(group.name)) {
                return groupMap[group.name]
            }
        }
        val result = database.create
                .select(RPKIT_CHARACTER_GROUP.PRIORITY)
                .from(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(group.name))
                .fetchOne() ?: return null
        val characterGroup = RPKCharacterGroup(
                character,
                group,
                result[RPKIT_CHARACTER_GROUP.PRIORITY]
        )
        val groupMap = cache?.get(characterGroup.character.id) as? MutableMap<String, RPKCharacterGroup> ?: mutableMapOf()
        groupMap[characterGroup.group.name] = characterGroup
        cache?.put(characterGroup.character.id, groupMap)
        return characterGroup
    }

    fun get(character: RPKCharacter): List<RPKCharacterGroup> = database.create
            .select(
                    RPKIT_CHARACTER_GROUP.GROUP_NAME,
                    RPKIT_CHARACTER_GROUP.PRIORITY
            )
            .from(RPKIT_CHARACTER_GROUP)
            .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(character.id))
            .orderBy(RPKIT_CHARACTER_GROUP.PRIORITY.desc())
            .fetch()
            .mapNotNull { result ->
                val group = result[RPKIT_CHARACTER_GROUP.GROUP_NAME]
                        .let { Services[RPKGroupService::class]?.getGroup(it) }
                        ?: return@mapNotNull null
                RPKCharacterGroup(
                        character,
                        group,
                        result[RPKIT_CHARACTER_GROUP.PRIORITY]
                )
            }

    fun delete(entity: RPKCharacterGroup) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(entity.group.name))
                .execute()
        val groupMap = cache?.get(entity.character.id) as? MutableMap<String, RPKCharacterGroup> ?: mutableMapOf()
        groupMap.remove(entity.group.name)
        cache?.put(entity.character.id, groupMap)
    }

}