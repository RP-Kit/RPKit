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
import com.rpkit.classes.bukkit.classes.RPKCharacterClass
import com.rpkit.classes.bukkit.classes.RPKClassName
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.classes.bukkit.database.create
import com.rpkit.classes.bukkit.database.jooq.Tables.RPKIT_CHARACTER_CLASS
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKCharacterClassTable(private val database: Database, private val plugin: RPKClassesBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_class.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-classes-bukkit.rpkit_character_class.character_id",
            Int::class.javaObjectType,
            RPKCharacterClass::class.java,
            plugin.config.getLong("caching.rpkit_character_class.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterClass): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CHARACTER_CLASS,
                    RPKIT_CHARACTER_CLASS.CHARACTER_ID,
                    RPKIT_CHARACTER_CLASS.CLASS_NAME
                )
                .values(
                    characterId.value,
                    entity.`class`.name.value
                )
                .execute()
            cache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert character class", exception)
            throw exception
        }
    }

    fun update(entity: RPKCharacterClass): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_CHARACTER_CLASS)
                .set(RPKIT_CHARACTER_CLASS.CLASS_NAME, entity.`class`.name.value)
                .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                .execute()
            cache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update character class", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter): CompletableFuture<RPKCharacterClass?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(characterId.value) == true) {
            return CompletableFuture.completedFuture(cache[characterId.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_CHARACTER_CLASS.CHARACTER_ID,
                        RPKIT_CHARACTER_CLASS.CLASS_NAME
                    )
                    .from(RPKIT_CHARACTER_CLASS)
                    .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                    .fetchOne() ?: return@supplyAsync null
                val classService = Services[RPKClassService::class.java] ?: return@supplyAsync null
                val className = result.get(RPKIT_CHARACTER_CLASS.CLASS_NAME)
                val `class` = classService.getClass(RPKClassName(className))
                return@supplyAsync if (`class` != null) {
                    val characterClass = RPKCharacterClass(
                        character,
                        `class`
                    )
                    cache?.set(characterId.value, characterClass)
                    characterClass
                } else {
                    database.create
                        .deleteFrom(RPKIT_CHARACTER_CLASS)
                        .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                        .execute()
                    null
                }
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get character class", exception)
                throw exception
            }
        }
    }

    fun delete(entity: RPKCharacterClass): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHARACTER_CLASS)
                .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                .execute()
            cache?.remove(characterId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete character class", exception)
            throw exception
        }
    }
}