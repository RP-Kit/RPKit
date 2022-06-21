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

package com.rpkit.economy.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.character.RPKMoneyHidden
import com.rpkit.economy.bukkit.database.create
import com.rpkit.economy.bukkit.database.jooq.Tables.RPKIT_MONEY_HIDDEN
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level
import java.util.logging.Level.SEVERE

/**
 * Represents the money hidden table.
 */
class RPKMoneyHiddenTable(
        private val database: Database,
        private val plugin: RPKEconomyBukkit
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

    fun insert(entity: RPKMoneyHidden): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_MONEY_HIDDEN,
                    RPKIT_MONEY_HIDDEN.CHARACTER_ID
                )
                .values(
                    characterId.value
                )
                .execute()
            characterCache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert money hidden", exception)
            throw exception
        }
    }

    /**
     * Gets the money hidden instance for a character.
     * If there is no money hidden row for the character, null is returned.
     *
     * @param character The character
     * @return The money hidden instance, or null if there is no money hidden instance for the character
     */
    operator fun get(character: RPKCharacter): CompletableFuture<RPKMoneyHidden?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        if (characterCache?.containsKey(characterId.value) == true) {
            return CompletableFuture.completedFuture(characterCache[characterId.value])
        }
        return CompletableFuture.supplyAsync {
            database.create
                .select(RPKIT_MONEY_HIDDEN.CHARACTER_ID)
                .from(RPKIT_MONEY_HIDDEN)
                .where(RPKIT_MONEY_HIDDEN.CHARACTER_ID.eq(characterId.value))
                .fetchOne() ?: return@supplyAsync null
            val moneyHidden = RPKMoneyHidden(character)
            characterCache?.set(characterId.value, moneyHidden)
            return@supplyAsync moneyHidden
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get money hidden", exception)
            throw exception
        }
    }

    fun delete(entity: RPKMoneyHidden): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_MONEY_HIDDEN)
                .where(RPKIT_MONEY_HIDDEN.CHARACTER_ID.eq(characterId.value))
                .execute()
            characterCache?.remove(characterId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete money hidden", exception)
            throw exception
        }
    }

    fun delete(characterId: RPKCharacterId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_MONEY_HIDDEN)
            .where(RPKIT_MONEY_HIDDEN.CHARACTER_ID.eq(characterId.value))
            .execute()
        characterCache?.remove(characterId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete money hidden for character id", exception)
        throw exception
    }

}
