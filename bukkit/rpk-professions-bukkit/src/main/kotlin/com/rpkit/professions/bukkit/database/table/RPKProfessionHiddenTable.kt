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
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.character.RPKProfessionHidden
import com.rpkit.professions.bukkit.database.jooq.Tables.RPKIT_PROFESSION_HIDDEN
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKProfessionHiddenTable(
        private val database: Database,
        val plugin: RPKProfessionsBukkit
) : Table {

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_profession_hidden.character_id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_profession_hidden.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfessionHidden::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_profession_hidden.character_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKProfessionHidden) {
        database.create
                .insertInto(
                        RPKIT_PROFESSION_HIDDEN,
                        RPKIT_PROFESSION_HIDDEN.CHARACTER_ID
                )
                .values(
                        entity.character.id
                )
                .execute()
        characterCache?.put(entity.character.id, entity)
    }

    fun update(entity: RPKProfessionHidden) {
        database.create
                .update(RPKIT_PROFESSION_HIDDEN)
                .set(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID, entity.character.id)
                .where(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID.eq(entity.character.id))
                .execute()
        characterCache?.put(entity.character.id, entity)
    }

    operator fun get(character: RPKCharacter): RPKProfessionHidden? {
        if (characterCache?.containsKey(character.id) == true) {
            return characterCache[character.id]
        }
        database.create
                .select(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID)
                .from(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        val professionHidden = RPKProfessionHidden(character)
        characterCache?.put(character.id, professionHidden)
        return professionHidden
    }

    fun delete(entity: RPKProfessionHidden) {
        database.create
                .deleteFrom(RPKIT_PROFESSION_HIDDEN)
                .where(RPKIT_PROFESSION_HIDDEN.CHARACTER_ID.eq(entity.character.id))
                .execute()
        characterCache?.remove(entity.character.id)
    }
}