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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguage
import com.rpkit.languages.bukkit.database.jooq.rpkit.Tables.RPKIT_CHARACTER_LANGUAGE
import com.rpkit.languages.bukkit.language.RPKLanguage
import com.rpkit.languages.bukkit.language.RPKLanguageProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType

class RPKCharacterLanguageTable(
        database: Database,
        private val plugin: RPKLanguagesBukkit
): Table<RPKCharacterLanguage>(database, RPKCharacterLanguage::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_language.id.enabled")) {
        database.cacheManager.createCache("rpk-languages-bukkit.rpkit_character_language.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterLanguage::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_language.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CHARACTER_LANGUAGE)
                .column(RPKIT_CHARACTER_LANGUAGE.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING, SQLDataType.DOUBLE)
                .constraints(
                        constraint("pk_rpkit_character_language").primaryKey(RPKIT_CHARACTER_LANGUAGE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.9.0")
        }
    }

    override fun insert(entity: RPKCharacterLanguage): Int {
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
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKCharacterLanguage) {
        database.create
                .update(RPKIT_CHARACTER_LANGUAGE)
                .set(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME, entity.language.name)
                .set(RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING, entity.understanding.toDouble())
                .where(RPKIT_CHARACTER_LANGUAGE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKCharacterLanguage? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID,
                        RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME,
                        RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING
                )
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.ID.eq(id))
                .fetchOne() ?: return null
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(result[RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID])
        val languageProvider = plugin.core.serviceManager.getServiceProvider(RPKLanguageProvider::class)
        val language = languageProvider.getLanguage(result[RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME])
        if (character == null || language == null) {
            database.create
                    .deleteFrom(RPKIT_CHARACTER_LANGUAGE)
                    .where(RPKIT_CHARACTER_LANGUAGE.ID.eq(id))
                    .execute()
            return null
        }
        val characterLanguage = RPKCharacterLanguage(
                id,
                character,
                language,
                result[RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING].toFloat()
        )
        cache?.put(id, characterLanguage)
        return characterLanguage
    }

    fun get(character: RPKCharacter, language: RPKLanguage): RPKCharacterLanguage? {
        val result = database.create
                .select(RPKIT_CHARACTER_LANGUAGE.ID)
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(
                        RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(character.id)
                                .and(RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME.eq(language.name))
                )
                .fetchOne() ?: return null
        return get(result[RPKIT_CHARACTER_LANGUAGE.ID])
    }

    fun get(character: RPKCharacter): List<RPKCharacterLanguage> {
        val results = database.create
                .select(RPKIT_CHARACTER_LANGUAGE.ID)
                .from(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID.eq(character.id))
                .fetch()
        return results.mapNotNull { result -> get(result[RPKIT_CHARACTER_LANGUAGE.ID]) }
    }

    override fun delete(entity: RPKCharacterLanguage) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_LANGUAGE)
                .where(RPKIT_CHARACTER_LANGUAGE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}