/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.banks.bukkit.database.table

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.bank.RPKBank
import com.rpkit.banks.bukkit.database.create
import com.rpkit.banks.bukkit.database.jooq.Tables.RPKIT_BANK
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import java.util.concurrent.CompletableFuture

/**
 * Represents the bank table.
 */
class RPKBankTable(private val database: Database, plugin: RPKBanksBukkit) : Table {

    private data class CharacterCurrencyCacheKey(
        val characterId: Int,
        val currencyName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_bank.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-banks-bukkit.rpkit_bank.character_id",
            CharacterCurrencyCacheKey::class.java,
            RPKBank::class.java,
            plugin.config.getLong("caching.rpkit_bank.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKBank): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val currencyName = entity.currency.name
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_BANK,
                    RPKIT_BANK.CHARACTER_ID,
                    RPKIT_BANK.CURRENCY_NAME,
                    RPKIT_BANK.BALANCE
                )
                .values(
                    characterId.value,
                    currencyName.value,
                    entity.balance
                )
                .execute()
            cache?.set(CharacterCurrencyCacheKey(characterId.value, currencyName.value), entity)
        }
    }

    fun update(entity: RPKBank): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val currencyName = entity.currency.name
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_BANK)
                .set(RPKIT_BANK.CURRENCY_NAME, currencyName.value)
                .set(RPKIT_BANK.BALANCE, entity.balance)
                .where(RPKIT_BANK.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_BANK.CURRENCY_NAME.eq(currencyName.value))
                .execute()
            cache?.set(CharacterCurrencyCacheKey(characterId.value, currencyName.value), entity)
        }
    }

    /**
     * Gets the bank account for the given character in the given currency.
     * If no account exists, one will be created.
     *
     * @param character The character to get the account for
     * @param currency The currency which the account should be in
     * @return The account of the character in the currency
     */
    operator fun get(character: RPKCharacter, currency: RPKCurrency): CompletableFuture<RPKBank?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val currencyName = currency.name
        val cacheKey = CharacterCurrencyCacheKey(characterId.value, currencyName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_BANK.BALANCE)
                .from(RPKIT_BANK)
                .where(RPKIT_BANK.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_BANK.CURRENCY_NAME.eq(currencyName.value))
                .fetchOne() ?: return@supplyAsync null
            val bank = RPKBank(
                character,
                currency,
                result.get(RPKIT_BANK.BALANCE)
            )
            cache?.set(cacheKey, bank)
            return@supplyAsync bank
        }
    }

    /**
     * Gets the characters with the highest balance in the given currency.
     *
     * @param amount The amount of characters to retrieve
     * @param currency The currency to
     * @return A list of characters with the highest balance in the given currency
     */
    fun getTop(amount: Int = 5, currency: RPKCurrency): CompletableFuture<List<RPKBank>> {
        return CompletableFuture.supplyAsync {
            val currencyName = currency.name
            val results = database.create
                .select(
                    RPKIT_BANK.CHARACTER_ID,
                    RPKIT_BANK.BALANCE
                )
                .from(RPKIT_BANK)
                .where(RPKIT_BANK.CURRENCY_NAME.eq(currencyName.value))
                .orderBy(RPKIT_BANK.BALANCE.desc())
                .limit(amount)
                .fetch()
            val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync emptyList()
            val banks = results
                .mapNotNull { result ->
                    val characterId = result[RPKIT_BANK.CHARACTER_ID]
                    val character = characterService.getCharacter(RPKCharacterId(characterId))
                    if (character == null) {
                        database.create.deleteFrom(RPKIT_BANK)
                            .where(RPKIT_BANK.CHARACTER_ID.eq(characterId))
                            .execute()
                        cache?.remove(CharacterCurrencyCacheKey(characterId, currencyName.value))
                        null
                    } else {
                        RPKBank(
                            character,
                            currency,
                            result[RPKIT_BANK.BALANCE]
                        )
                    }
                }
            banks.forEach { bank ->
                val characterId = bank.character.id
                if (characterId != null) {
                    cache?.set(CharacterCurrencyCacheKey(characterId.value, currencyName.value), bank)
                }
            }
            return@supplyAsync banks
        }
    }

    fun delete(entity: RPKBank): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val currencyName = entity.currency.name
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_BANK)
                .where(RPKIT_BANK.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_BANK.CURRENCY_NAME.eq(currencyName.value))
                .execute()
            cache?.remove(CharacterCurrencyCacheKey(characterId.value, currencyName.value))
        }
    }

}