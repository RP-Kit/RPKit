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

package com.rpkit.permissions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.create
import com.rpkit.permissions.bukkit.database.jooq.Tables.RPKIT_CHARACTER_GROUP
import com.rpkit.permissions.bukkit.group.RPKCharacterGroup
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupService


class RPKCharacterGroupTable(private val database: Database, private val plugin: RPKPermissionsBukkit) : Table {

    private data class CharacterGroupCacheKey(
        val characterId: Int,
        val groupName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_group.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-permissions-bukkit.rpkit_character_group.character_id",
            CharacterGroupCacheKey::class.java,
            RPKCharacterGroup::class.java,
            plugin.config.getLong("caching.rpkit_character_group.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterGroup) {
        val characterId = entity.character.id ?: return
        val groupName = entity.group.name
        database.create
                .insertInto(
                        RPKIT_CHARACTER_GROUP,
                        RPKIT_CHARACTER_GROUP.CHARACTER_ID,
                        RPKIT_CHARACTER_GROUP.GROUP_NAME,
                        RPKIT_CHARACTER_GROUP.PRIORITY
                )
                .values(
                        characterId.value,
                        groupName,
                        entity.priority
                )
                .execute()
        cache?.set(CharacterGroupCacheKey(characterId.value, groupName), entity)
    }

    fun update(entity: RPKCharacterGroup) {
        val characterId = entity.character.id ?: return
        val groupName = entity.group.name
        database.create
                .update(RPKIT_CHARACTER_GROUP)
                .set(RPKIT_CHARACTER_GROUP.PRIORITY, entity.priority)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(entity.group.name))
                .execute()
        cache?.set(CharacterGroupCacheKey(characterId.value, groupName), entity)
    }

    operator fun get(character: RPKCharacter, group: RPKGroup): RPKCharacterGroup? {
        val characterId = character.id ?: return null
        val groupName = group.name
        val cacheKey = CharacterGroupCacheKey(characterId.value, groupName)
        if (cache?.containsKey(cacheKey) == true) {
            return cache[cacheKey]
        }
        val result = database.create
                .select(RPKIT_CHARACTER_GROUP.PRIORITY)
                .from(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(group.name))
                .fetchOne() ?: return null
        val characterGroup = RPKCharacterGroup(
                character,
                group,
                result[RPKIT_CHARACTER_GROUP.PRIORITY]
        )
        cache?.set(cacheKey, characterGroup)
        return characterGroup
    }

    fun get(character: RPKCharacter): List<RPKCharacterGroup> {
        val characterId = character.id ?: return emptyList()
        return database.create
            .select(
                RPKIT_CHARACTER_GROUP.GROUP_NAME,
                RPKIT_CHARACTER_GROUP.PRIORITY
            )
            .from(RPKIT_CHARACTER_GROUP)
            .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
            .orderBy(RPKIT_CHARACTER_GROUP.PRIORITY.desc())
            .fetch()
            .mapNotNull { result ->
                val group = result[RPKIT_CHARACTER_GROUP.GROUP_NAME]
                    .let { Services[RPKGroupService::class.java]?.getGroup(it) }
                    ?: return@mapNotNull null
                RPKCharacterGroup(
                    character,
                    group,
                    result[RPKIT_CHARACTER_GROUP.PRIORITY]
                )
            }
    }

    fun delete(entity: RPKCharacterGroup) {
        val characterId = entity.character.id ?: return
        val groupName = entity.group.name
        database.create
                .deleteFrom(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(entity.group.name))
                .execute()
        cache?.set(CharacterGroupCacheKey(characterId.value, groupName), entity)
    }

}