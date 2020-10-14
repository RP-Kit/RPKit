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
import com.rpkit.core.service.Services
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.jooq.Tables.RPKIT_CHARACTER_PROFESSION
import com.rpkit.professions.bukkit.profession.RPKCharacterProfession
import com.rpkit.professions.bukkit.profession.RPKProfession
import com.rpkit.professions.bukkit.profession.RPKProfessionService


class RPKCharacterProfessionTable(
        private val database: Database,
        val plugin: RPKProfessionsBukkit
) : Table {

    fun insert(entity: RPKCharacterProfession) {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_PROFESSION,
                        RPKIT_CHARACTER_PROFESSION.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION.PROFESSION
                )
                .values(
                        entity.character.id,
                        entity.profession.name
                )
                .execute()
    }

    fun update(entity: RPKCharacterProfession) {
        database.create
                .update(RPKIT_CHARACTER_PROFESSION)
                .set(RPKIT_CHARACTER_PROFESSION.PROFESSION, entity.profession.name)
                .where(RPKIT_CHARACTER_PROFESSION.CHARACTER_ID.eq(entity.character.id))
                .execute()
    }

    operator fun get(character: RPKCharacter): List<RPKCharacterProfession> {
        val results = database.create
                .select(RPKIT_CHARACTER_PROFESSION.CHARACTER_ID)
                .from(RPKIT_CHARACTER_PROFESSION)
                .where(RPKIT_CHARACTER_PROFESSION.CHARACTER_ID.eq(character.id))
                .fetch() ?: return emptyList()
        val professionService = Services[RPKProfessionService::class] ?: return emptyList()
        return results.mapNotNull { result ->
            val profession = professionService.getProfession(result[RPKIT_CHARACTER_PROFESSION.PROFESSION])
            if (profession == null) {
                database.create
                        .deleteFrom(RPKIT_CHARACTER_PROFESSION)
                        .where(RPKIT_CHARACTER_PROFESSION.PROFESSION.eq(result[RPKIT_CHARACTER_PROFESSION.PROFESSION]))
                        .execute()
                return@mapNotNull null
            }
            return@mapNotNull RPKCharacterProfession(
                    character,
                    profession
            )
        }
    }

    fun get(character: RPKCharacter, profession: RPKProfession): RPKCharacterProfession? {
        database.create
                .select(
                        RPKIT_CHARACTER_PROFESSION.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION.PROFESSION
                )
                .from(RPKIT_CHARACTER_PROFESSION)
                .where(RPKIT_CHARACTER_PROFESSION.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_PROFESSION.PROFESSION.eq(profession.name))
                .fetchOne() ?: return null
        return RPKCharacterProfession(character, profession)
    }

    fun delete(entity: RPKCharacterProfession) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION)
                .where(RPKIT_CHARACTER_PROFESSION.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_CHARACTER_PROFESSION.PROFESSION.eq(entity.profession.name))
                .execute()
    }
}