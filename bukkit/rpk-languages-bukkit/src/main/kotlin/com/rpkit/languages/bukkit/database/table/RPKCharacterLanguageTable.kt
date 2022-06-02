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
import com.rpkit.languages.bukkit.language.RPKLanguageName
import com.rpkit.languages.bukkit.language.RPKLanguageService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

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

    fun insert(entity: RPKCharacterLanguage): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val languageName = entity.language.name
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CHARACTER_LANGUAGE,
                    RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID,
                    RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME,
                    RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING
                )
                .values(
                    characterId.value,
                    entity.language.name.value,
                    entity.understanding.toDouble()
                )
                .execute()
            cache?.set(CharacterLanguageCacheKey(characterId.value, languageName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert character language", exception)
            throw exception
        }
    }

    fun update(entity: RPKCharacterLanguage): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val languageName = entity.language.name
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_CHARACTER_LANGUAGE)
                .set(RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING, entity.understanding.toDouble())
                .where(
                    RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(characterId.value)
                        .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(languageName.value))
                )
                .execute()
            cache?.set(CharacterLanguageCacheKey(characterId.value, languageName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update character language", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter, language: RPKLanguage): CompletableFuture<RPKCharacterLanguage?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val languageName = language.name
        val cacheKey = CharacterLanguageCacheKey(characterId.value, languageName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID,
                    RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME,
                    RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING
                )
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(language.name.value))
                .fetchOne() ?: return@supplyAsync null
            val characterLanguage = RPKCharacterLanguage(
                character,
                language,
                result[RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING].toFloat()
            )
            cache?.set(cacheKey, characterLanguage)
            return@supplyAsync characterLanguage
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character language", exception)
            throw exception
        }
    }

    fun get(character: RPKCharacter): CompletableFuture<List<RPKCharacterLanguage>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME)
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(characterId.value))
                .fetch()
            val languageService = Services[RPKLanguageService::class.java] ?: return@supplyAsync emptyList()
            val languageFutures = results.mapNotNull { result ->
                languageService.getLanguage(RPKLanguageName(result[RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME]))?.let { language ->
                    get(character, language)
                }
            }
            CompletableFuture.allOf(*languageFutures.toTypedArray()).join()
            return@supplyAsync languageFutures.mapNotNull(CompletableFuture<RPKCharacterLanguage?>::join)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character languages", exception)
            throw exception
        }
    }

    fun delete(entity: RPKCharacterLanguage): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val languageName = entity.language.name
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(languageName.value))
                .execute()
            cache?.remove(CharacterLanguageCacheKey(characterId.value, languageName.value))
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete character language", exception)
            throw exception
        }
    }

}