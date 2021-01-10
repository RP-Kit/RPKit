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

package com.rpkit.professions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.character.RPKProfessionHidden
import com.rpkit.professions.bukkit.database.create
import com.rpkit.professions.bukkit.database.jooq.Tables.RPKIT_PROFESSION_HIDDEN


class RPKProfessionHiddenTable(
        private val database: Database,
        val plugin: RPKProfessionsBukkit
) : Table {

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_profession_hidden.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-professions-bukkit.rpkit_profession_hidden.character_id",
            Int::class.javaObjectType,
            RPKProfessionHidden::class.java,
            plugin.config.getLong("caching.rpkit_profession_hidden.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKProfessionHidden) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_PROFESSION_HIDDEN,
                        RPKIT_PROFESSION_HIDDEN.CHARACTER_ID
                )
                .values(
                    characterId.value
                )
                .execute()
        characterCache?.set(characterId.value, entity)
    }

    operator fun get(character: RPKCharacter): RPKProfessionHidden? {
        val characterId = character.id ?: return null
        if (characterCache?.containsKey(characterId.value) == true) {
            return characterCache[characterId.value]
        }
        database.create
                .select(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID)
                .from(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID.eq(characterId.value))
                .fetchOne() ?: return null
        val professionHidden = RPKProfessionHidden(character)
        characterCache?.set(characterId.value, professionHidden)
        return professionHidden
    }

    fun delete(entity: RPKProfessionHidden) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID.eq(characterId.value))
                .execute()
        characterCache?.remove(characterId.value)
    }
}