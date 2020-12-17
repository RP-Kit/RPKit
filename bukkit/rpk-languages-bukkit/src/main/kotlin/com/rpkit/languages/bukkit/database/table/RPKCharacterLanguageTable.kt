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

package com.rpkit.languages.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguage
import com.rpkit.languages.bukkit.database.create
import com.rpkit.languages.bukkit.database.jooq.Tables.RPKIT_CHARACTER_LANGUAGE
import com.rpkit.languages.bukkit.language.RPKLanguage
import com.rpkit.languages.bukkit.language.RPKLanguageService

class RPKCharacterLanguageTable(
        private val database: Database,
        private val plugin: RPKLanguagesBukkit
) : Table {

    private data class CharacterLanguageCacheKey(
        val characterId: Int,
        val languageName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_language.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-languages-bukkit.rpkit_character_language.character_id",
            CharacterLanguageCacheKey::class.java,
            RPKCharacterLanguage::class.java,
            plugin.config.getLong("caching.rpkit_character_language.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterLanguage) {
        val characterId = entity.character.id ?: return
        val languageName = entity.language.name
        database.create
                .insertInto(
                        RPKIT_CHARACTER_LANGUAGE,
                        RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID,
                        RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME,
                        RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING
                )
                .values(
                        entity.character.id,
                        entity.language.name,
                        entity.understanding.toDouble()
                )
                .execute()
        cache?.set(CharacterLanguageCacheKey(characterId, languageName), entity)
    }

    fun update(entity: RPKCharacterLanguage) {
        val characterId = entity.character.id ?: return
        val languageName = entity.language.name
        database.create
                .update(RPKIT_CHARACTER_LANGUAGE)
                .set(RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING, entity.understanding.toDouble())
                .where(
                        RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(characterId)
                                .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(languageName))
                )
                .execute()
        cache?.set(CharacterLanguageCacheKey(characterId, languageName), entity)
    }

    operator fun get(character: RPKCharacter, language: RPKLanguage): RPKCharacterLanguage? {
        val characterId = character.id ?: return null
        val languageName = language.name
        val cacheKey = CharacterLanguageCacheKey(characterId, languageName)
        if (cache?.containsKey(cacheKey) == true) {
            return cache[cacheKey]
        }
        val result = database.create
                .select(
                        RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID,
                        RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME,
                        RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING
                )
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(language.name))
                .fetchOne() ?: return null
        val characterLanguage = RPKCharacterLanguage(
                character,
                language,
                result[RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING].toFloat()
        )
        cache?.set(cacheKey, characterLanguage)
        return characterLanguage
    }

    fun get(character: RPKCharacter): List<RPKCharacterLanguage> {
        val results = database.create
                .select(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME)
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(character.id))
                .fetch()
        val languageService = Services[RPKLanguageService::class.java] ?: return emptyList()
        return results.mapNotNull { result ->
            val language = languageService.getLanguage(result[RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME]) ?: return@mapNotNull null
            return@mapNotNull get(character, language)
        }
    }

    fun delete(entity: RPKCharacterLanguage) {
        val characterId = entity.character.id ?: return
        val languageName = entity.language.name
        database.create
                .deleteFrom(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(characterId))
                .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(languageName))
                .execute()
        cache?.remove(CharacterLanguageCacheKey(characterId, languageName))
    }

}