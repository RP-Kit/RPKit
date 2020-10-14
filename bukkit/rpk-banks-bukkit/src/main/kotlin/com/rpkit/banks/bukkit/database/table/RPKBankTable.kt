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

package com.rpkit.banks.bukkit.database.table

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.bank.RPKBank
import com.rpkit.banks.bukkit.database.jooq.Tables.RPKIT_BANK
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the bank table.
 */
class RPKBankTable(private val database: Database, plugin: RPKBanksBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_bank.character_id")) {
        database.cacheManager.createCache("rpk-banks-bukkit.rpkit_bank.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                ResourcePoolsBuilder.heap(20L)))
    } else {
        null
    }

    fun insert(entity: RPKBank) {
        database.create
                .insertInto(
                        RPKIT_BANK,
                        RPKIT_BANK.CHARACTER_ID,
                        RPKIT_BANK.CURRENCY_ID,
                        RPKIT_BANK.BALANCE
                )
                .values(
                        entity.character.id,
                        entity.currency.id,
                        entity.balance
                )
                .execute()
        updateCache(entity)
    }

    fun update(entity: RPKBank) {
        database.create
                .update(RPKIT_BANK)
                .set(RPKIT_BANK.CURRENCY_ID, entity.currency.id)
                .set(RPKIT_BANK.BALANCE, entity.balance)
                .where(RPKIT_BANK.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_BANK.CURRENCY_ID.eq(entity.currency.id))
                .execute()
        updateCache(entity)
    }

    /**
     * Gets the bank account for the given character in the given currency.
     * If no account exists, one will be created.
     *
     * @param character The character to get the account for
     * @param currency The currency which the account should be in
     * @return The account of the character in the currency
     */
    operator fun get(character: RPKCharacter, currency: RPKCurrency): RPKBank? {
        if (cache?.containsKey(character.id) == true) {
            val currencyMap = cache[character.id] as? MutableMap<Int, RPKBank>
            if (currencyMap != null) {
                if (currencyMap.containsKey(currency.id)) {
                    return currencyMap[currency.id] as RPKBank
                }
            }
        }
        val result = database.create
                .select(RPKIT_BANK.BALANCE)
                .from(RPKIT_BANK)
                .where(RPKIT_BANK.CHARACTER_ID.eq(character.id))
                .and(RPKIT_BANK.CURRENCY_ID.eq(currency.id))
                .fetchOne() ?: return null
        val bank = RPKBank(
                character,
                currency,
                result.get(RPKIT_BANK.BALANCE)
        )
        updateCache(bank)
        return bank
    }

    /**
     * Gets the characters with the highest balance in the given currency.
     *
     * @param amount The amount of characters to retrieve
     * @param currency The currency to
     * @return A list of characters with the highest balance in the given currency
     */
    fun getTop(amount: Int = 5, currency: RPKCurrency): List<RPKBank> {
        val results = database.create
                .select(
                        RPKIT_BANK.CHARACTER_ID,
                        RPKIT_BANK.BALANCE
                )
                .from(RPKIT_BANK)
                .where(RPKIT_BANK.CURRENCY_ID.eq(currency.id))
                .orderBy(RPKIT_BANK.BALANCE.desc())
                .limit(amount)
                .fetch()
        val characterService = Services[RPKCharacterService::class] ?: return emptyList()
        val banks = results
                .mapNotNull { result ->
                    val character = characterService.getCharacter(result[RPKIT_BANK.CHARACTER_ID])
                    if (character == null) {
                        database.create.deleteFrom(RPKIT_BANK)
                                .where(RPKIT_BANK.CHARACTER_ID.eq(result[RPKIT_BANK.CHARACTER_ID]))
                                .execute()
                        cache?.remove(result[RPKIT_BANK.CHARACTER_ID])
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
            updateCache(bank)
        }
        return banks
    }

    fun delete(entity: RPKBank) {
        database.create
                .deleteFrom(RPKIT_BANK)
                .where(RPKIT_BANK.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_BANK.CURRENCY_ID.eq(entity.currency.id))
                .execute()
        if (cache != null) {
            val currencyMap = cache[entity.character.id]
            currencyMap.remove(entity.currency.id)
            if (currencyMap.isEmpty()) {
                cache.remove(entity.character.id)
            } else {
                cache.put(entity.character.id, currencyMap)
            }
        }
    }

    private fun updateCache(entity: RPKBank) {
        if (cache != null) {
            val currencyMap = cache[entity.character.id] as? MutableMap<Int, RPKBank> ?: mutableMapOf()
            val currencyId = entity.currency.id ?: return
            currencyMap[currencyId] = entity
            cache.put(entity.character.id, currencyMap)
        }
    }

}