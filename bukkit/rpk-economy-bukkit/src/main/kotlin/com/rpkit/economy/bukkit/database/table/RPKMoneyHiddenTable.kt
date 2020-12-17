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

package com.rpkit.economy.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.character.RPKMoneyHidden
import com.rpkit.economy.bukkit.database.create
import com.rpkit.economy.bukkit.database.jooq.Tables.RPKIT_MONEY_HIDDEN

/**
 * Represents the money hidden table.
 */
class RPKMoneyHiddenTable(
        private val database: Database,
        plugin: RPKEconomyBukkit
) : Table {

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_money_hidden.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-economy-bukkit.rpkit_money_hidden.character_id",
            Int::class.javaObjectType,
            RPKMoneyHidden::class.java,
            plugin.config.getLong("caching.rpkit_money_hidden.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKMoneyHidden) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_MONEY_HIDDEN,
                        RPKIT_MONEY_HIDDEN.CHARACTER_ID
                )
                .values(
                    characterId
                )
                .execute()
        characterCache?.set(characterId, entity)
    }

    /**
     * Gets the money hidden instance for a character.
     * If there is no money hidden row for the character, null is returned.
     *
     * @param character The character
     * @return The money hidden instance, or null if there is no money hidden instance for the character
     */
    operator fun get(character: RPKCharacter): RPKMoneyHidden? {
        val characterId = character.id ?: return null
        if (characterCache?.containsKey(characterId) == true) {
            return characterCache[characterId]
        } else {
            database.create
                    .select(RPKIT_MONEY_HIDDEN.CHARACTER_ID)
                    .from(RPKIT_MONEY_HIDDEN)
                    .where(RPKIT_MONEY_HIDDEN.CHARACTER_ID.eq(characterId))
                    .fetchOne() ?: return null
            val moneyHidden = RPKMoneyHidden(character)
            characterCache?.set(characterId, moneyHidden)
            return moneyHidden
        }
    }

    fun delete(entity: RPKMoneyHidden) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_MONEY_HIDDEN)
                .where(RPKIT_MONEY_HIDDEN.CHARACTER_ID.eq(characterId))
                .execute()
        characterCache?.remove(characterId)
    }

}
