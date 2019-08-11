/*
 * Copyright 2019 Ren Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.jooq.rpkit.Tables.RPKIT_CHARACTER_PROFESSION_EXPERIENCE
import com.rpkit.professions.bukkit.profession.RPKCharacterProfessionExperience
import com.rpkit.professions.bukkit.profession.RPKProfession
import com.rpkit.professions.bukkit.profession.RPKProfessionProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKCharacterProfessionExperienceTable(
        database: Database,
        val plugin: RPKProfessionsBukkit
): Table<RPKCharacterProfessionExperience>(
        database,
        RPKCharacterProfessionExperience::class
) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_profession_experience.id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_character_profession_experience.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterProfessionExperience::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_profession_experience.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .column(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION, SQLDataType.VARCHAR(256))
                .column(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_character_profession_experience").primaryKey(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.7.0")
        }
    }

    override fun insert(entity: RPKCharacterProfessionExperience): Int {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE
                )
                .values(
                        entity.character.id,
                        entity.profession.name,
                        entity.experience
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKCharacterProfessionExperience) {
        database.create
                .update(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .set(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION, entity.profession.name)
                .set(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKCharacterProfessionExperience? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION,
                        RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE
                )
                .from(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID.eq(id))
                .fetchOne() ?: return null
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(result[RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID])
        if (character == null) {
            database.create
                    .deleteFrom(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                    .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        val profession = professionProvider.getProfession(result[RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION])
        if (profession == null) {
            database.create
                    .deleteFrom(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                    .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val characterProfessionExperience = RPKCharacterProfessionExperience(
                id,
                character,
                profession,
                result[RPKIT_CHARACTER_PROFESSION_EXPERIENCE.EXPERIENCE]
        )
        cache?.put(id, characterProfessionExperience)
        return characterProfessionExperience
    }

    fun get(character: RPKCharacter): List<RPKCharacterProfessionExperience> {
        val results = database.create
                .select(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID)
                .from(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .fetch()
        return results.mapNotNull { result -> get(result[RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID]) }
    }

    fun get(character: RPKCharacter, profession: RPKProfession): RPKCharacterProfessionExperience? {
        val result = database.create
                .select(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID)
                .from(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION.eq(profession.name))
                .fetchOne() ?: return null
        return get(result[RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID])
    }

    override fun delete(entity: RPKCharacterProfessionExperience) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION_EXPERIENCE)
                .where(RPKIT_CHARACTER_PROFESSION_EXPERIENCE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}