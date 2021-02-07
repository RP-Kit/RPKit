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
import com.rpkit.classes.bukkit.classes.RPKCharacterClass
import com.rpkit.classes.bukkit.classes.RPKClassName
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.classes.bukkit.database.create
import com.rpkit.classes.bukkit.database.jooq.Tables.RPKIT_CHARACTER_CLASS
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services


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

    fun insert(entity: RPKCharacterClass) {
        val characterId = entity.character.id ?: return
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
    }

    fun update(entity: RPKCharacterClass) {
        val characterId = entity.character.id ?: return
        database.create
                .update(RPKIT_CHARACTER_CLASS)
                .set(RPKIT_CHARACTER_CLASS.CLASS_NAME, entity.`class`.name.value)
                .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                .execute()
        cache?.set(characterId.value, entity)
    }

    operator fun get(character: RPKCharacter): RPKCharacterClass? {
        val characterId = character.id ?: return null
        if (cache?.containsKey(characterId.value) == true) {
            return cache[characterId.value]
        } else {
            val result = database.create
                    .select(
                            RPKIT_CHARACTER_CLASS.CHARACTER_ID,
                            RPKIT_CHARACTER_CLASS.CLASS_NAME
                    )
                    .from(RPKIT_CHARACTER_CLASS)
                    .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                    .fetchOne() ?: return null
            val classService = Services[RPKClassService::class.java] ?: return null
            val className = result.get(RPKIT_CHARACTER_CLASS.CLASS_NAME)
            val `class` = classService.getClass(RPKClassName(className))
            return if (`class` != null) {
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
        }
    }

    fun delete(entity: RPKCharacterClass) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_CHARACTER_CLASS)
                .where(RPKIT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
                .execute()
        cache?.remove(characterId.value)
    }
}