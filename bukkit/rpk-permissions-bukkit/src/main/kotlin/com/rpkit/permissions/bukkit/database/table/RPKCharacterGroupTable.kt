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
import com.rpkit.permissions.bukkit.group.RPKGroupName
import com.rpkit.permissions.bukkit.group.RPKGroupService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKCharacterGroupTable(private val database: Database, private val plugin: RPKPermissionsBukkit) : Table {

    data class CharacterGroupCacheKey(
        val characterId: Int,
        val groupName: String
    )

    val cache = if (plugin.config.getBoolean("caching.rpkit_character_group.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-permissions-bukkit.rpkit_character_group.character_id",
            CharacterGroupCacheKey::class.java,
            RPKCharacterGroup::class.java,
            plugin.config.getLong("caching.rpkit_character_group.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterGroup): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val groupName = entity.group.name
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CHARACTER_GROUP,
                    RPKIT_CHARACTER_GROUP.CHARACTER_ID,
                    RPKIT_CHARACTER_GROUP.GROUP_NAME,
                    RPKIT_CHARACTER_GROUP.PRIORITY
                )
                .values(
                    characterId.value,
                    groupName.value,
                    entity.priority
                )
                .execute()
            cache?.set(CharacterGroupCacheKey(characterId.value, groupName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert character group", exception)
            throw exception
        }
    }

    fun update(entity: RPKCharacterGroup): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val groupName = entity.group.name
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_CHARACTER_GROUP)
                .set(RPKIT_CHARACTER_GROUP.PRIORITY, entity.priority)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(entity.group.name.value))
                .execute()
            cache?.set(CharacterGroupCacheKey(characterId.value, groupName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update character group", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter, group: RPKGroup): CompletableFuture<RPKCharacterGroup?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val groupName = group.name
        val cacheKey = CharacterGroupCacheKey(characterId.value, groupName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_CHARACTER_GROUP.PRIORITY)
                .from(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(groupName.value))
                .fetchOne() ?: return@supplyAsync null
            val characterGroup = RPKCharacterGroup(
                character,
                group,
                result[RPKIT_CHARACTER_GROUP.PRIORITY]
            )
            cache?.set(cacheKey, characterGroup)
            return@supplyAsync characterGroup
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character group", exception)
            throw exception
        }
    }

    fun get(character: RPKCharacter): CompletableFuture<List<RPKCharacterGroup>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            return@supplyAsync database.create
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
                        .let { Services[RPKGroupService::class.java]?.getGroup(RPKGroupName(it)) }
                        ?: return@mapNotNull null
                    RPKCharacterGroup(
                        character,
                        group,
                        result[RPKIT_CHARACTER_GROUP.PRIORITY]
                    )
                }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character groups", exception)
            throw exception
        }
    }

    fun delete(entity: RPKCharacterGroup): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val groupName = entity.group.name
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_GROUP.GROUP_NAME.eq(groupName.value))
                .execute()
            cache?.set(CharacterGroupCacheKey(characterId.value, groupName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete character group", exception)
            throw exception
        }
    }

}