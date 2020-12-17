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

package com.rpkit.drinks.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.drinks.bukkit.RPKDrinksBukkit
import com.rpkit.drinks.bukkit.database.create
import com.rpkit.drinks.bukkit.database.jooq.Tables.RPKIT_DRUNKENNESS
import com.rpkit.drinks.bukkit.drink.RPKDrunkenness


class RPKDrunkennessTable(private val database: Database, private val plugin: RPKDrinksBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_drunkenness.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-drinks-bukkit.rpkit_drunkenness.character_id",
            Int::class.javaObjectType,
            RPKDrunkenness::class.java,
            plugin.config.getLong("caching.rpkit_drunkenness.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKDrunkenness) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_DRUNKENNESS,
                        RPKIT_DRUNKENNESS.CHARACTER_ID,
                        RPKIT_DRUNKENNESS.DRUNKENNESS
                )
                .values(
                        characterId,
                        entity.drunkenness
                )
                .execute()
        cache?.set(characterId, entity)
    }

    fun update(entity: RPKDrunkenness) {
        val characterId = entity.character.id ?: return
        database.create
                .update(RPKIT_DRUNKENNESS)
                .set(RPKIT_DRUNKENNESS.DRUNKENNESS, entity.drunkenness)
                .where(RPKIT_DRUNKENNESS.CHARACTER_ID.eq(characterId))
                .execute()
        cache?.set(characterId, entity)
    }

    operator fun get(character: RPKCharacter): RPKDrunkenness? {
        val characterId = character.id ?: return null
        if (cache?.containsKey(characterId) == true) {
            return cache[characterId]
        } else {
            val result = database.create
                    .select(
                            RPKIT_DRUNKENNESS.CHARACTER_ID,
                            RPKIT_DRUNKENNESS.DRUNKENNESS
                    )
                    .from(RPKIT_DRUNKENNESS)
                    .where(RPKIT_DRUNKENNESS.CHARACTER_ID.eq(characterId))
                    .fetchOne() ?: return null
            val drunkenness = RPKDrunkenness(
                    character,
                    result.get(RPKIT_DRUNKENNESS.DRUNKENNESS)
            )
            cache?.set(characterId, drunkenness)
            return drunkenness
        }
    }

    fun delete(entity: RPKDrunkenness) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_DRUNKENNESS)
                .where(RPKIT_DRUNKENNESS.CHARACTER_ID.eq(characterId))
                .execute()
        cache?.remove(characterId)
    }

}