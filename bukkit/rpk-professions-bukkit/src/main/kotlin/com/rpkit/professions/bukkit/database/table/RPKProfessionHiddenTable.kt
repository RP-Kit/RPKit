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
import com.rpkit.professions.bukkit.character.RPKProfessionHidden
import com.rpkit.professions.bukkit.database.jooq.rpkit.Tables.RPKIT_PROFESSION_HIDDEN
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKProfessionHiddenTable(
        database: Database,
        val plugin: RPKProfessionsBukkit
): Table<RPKProfessionHidden>(database, RPKProfessionHidden::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_profession_hidden.id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_profession_hidden.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfessionHidden::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_profession_hidden.id.size"))).build())
    } else {
        null
    }

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_profession_hidden.character_id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_profession_hidden.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfessionHidden::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_profession_hidden.character_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PROFESSION_HIDDEN)
                .column(RPKIT_PROFESSION_HIDDEN.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_profession_hidden").primaryKey(RPKIT_PROFESSION_HIDDEN.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.7.0")
        }
    }

    override fun insert(entity: RPKProfessionHidden): Int {
        database.create
                .insertInto(
                        RPKIT_PROFESSION_HIDDEN,
                        RPKIT_PROFESSION_HIDDEN.CHARACTER_ID
                )
                .values(
                        entity.character.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        characterCache?.put(entity.character.id, entity)
        return id
    }

    override fun update(entity: RPKProfessionHidden) {
        database.create
                .update(RPKIT_PROFESSION_HIDDEN)
                .set(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID, entity.character.id)
                .where(RPKIT_PROFESSION_HIDDEN.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        characterCache?.put(entity.character.id, entity)
    }

    override fun get(id: Int): RPKProfessionHidden? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID)
                .from(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.ID.eq(id))
                .fetchOne() ?: return null
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(result[RPKIT_PROFESSION_HIDDEN.CHARACTER_ID])
        if (character == null) {
            database.create
                    .deleteFrom(RPKIT_PROFESSION_HIDDEN)
                    .where(RPKIT_PROFESSION_HIDDEN.ID.eq(id))
                    .execute()
            cache?.remove(id)
            characterCache?.remove(result[RPKIT_PROFESSION_HIDDEN.CHARACTER_ID])
            return null
        }
        val professionHidden = RPKProfessionHidden(
                id,
                character
        )
        cache?.put(id, professionHidden)
        characterCache?.put(professionHidden.character.id, professionHidden)
        return professionHidden
    }

    fun get(character: RPKCharacter): RPKProfessionHidden? {
        if (characterCache?.containsKey(character.id) == true) {
            return characterCache[character.id]
        }
        val result = database.create
                .select(RPKIT_PROFESSION_HIDDEN.ID)
                .from(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        val professionHidden = RPKProfessionHidden(
                result[RPKIT_PROFESSION_HIDDEN.ID],
                character
        )
        cache?.put(professionHidden.id, professionHidden)
        characterCache?.put(character.id, professionHidden)
        return professionHidden
    }

    override fun delete(entity: RPKProfessionHidden) {
        database.create
                .deleteFrom(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        characterCache?.remove(entity.character.id)
    }
}