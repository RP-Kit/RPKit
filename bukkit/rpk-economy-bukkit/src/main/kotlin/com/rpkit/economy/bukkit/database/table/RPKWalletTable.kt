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
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.database.jooq.Tables.RPKIT_WALLET
import com.rpkit.economy.bukkit.wallet.RPKWallet
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the wallet table.
 */
class RPKWalletTable(
        private val database: Database,
        plugin: RPKEconomyBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_wallet.character_id.enabled")) {
        database.cacheManager.createCache("rpk-economy-bukkit.rpkit_wallet.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Int::class.javaObjectType,
                        MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_wallet.character_id.size"))
                ).build()
        )
    } else {
        null
    }

    fun insert(entity: RPKWallet) {
        database.create
                .insertInto(
                        RPKIT_WALLET,
                        RPKIT_WALLET.CHARACTER_ID,
                        RPKIT_WALLET.CURRENCY_ID,
                        RPKIT_WALLET.BALANCE
                )
                .values(
                        entity.character.id,
                        entity.currency.id,
                        entity.balance
                )
                .execute()
        cacheInsert(entity)
    }

    fun update(entity: RPKWallet) {
        database.create
                .update(RPKIT_WALLET)
                .set(RPKIT_WALLET.BALANCE, entity.balance)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_WALLET.CURRENCY_ID.eq(entity.currency.id))
                .execute()
        cacheInsert(entity)
    }

    fun get(character: RPKCharacter, currency: RPKCurrency): RPKWallet? {
        val characterId = character.id
        val currencyId = currency.id
        if (characterId == null || currencyId == null) return null
        if (cache?.containsKey(characterId) == true) {
            val currencyWallets = cache[characterId] as? MutableMap<Int, RPKWallet>
            if (currencyWallets?.containsKey(currencyId) == true) {
                return currencyWallets[currencyId]
            }
        }
        val result = database.create
                .select(RPKIT_WALLET.BALANCE)
                .from(RPKIT_WALLET)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(character.id))
                .and(RPKIT_WALLET.CURRENCY_ID.eq(currency.id))
                .fetchOne() ?: return null
        val wallet = RPKWallet(
                character,
                currency,
                result[RPKIT_WALLET.BALANCE]
        )
        cacheInsert(wallet)
        return wallet
    }

    fun getTop(amount: Int = 5, currency: RPKCurrency): List<RPKWallet> {
        val results = database.create
                .select(
                        RPKIT_WALLET.CHARACTER_ID,
                        RPKIT_WALLET.BALANCE
                )
                .from(RPKIT_WALLET)
                .where(RPKIT_WALLET.CURRENCY_ID.eq(currency.id))
                .orderBy(RPKIT_WALLET.BALANCE.desc())
                .limit(amount)
                .fetch()
        val characterService = Services[RPKCharacterService::class] ?: return emptyList()
        return results
                .mapNotNull { result ->
                    val characterId = result[RPKIT_WALLET.CHARACTER_ID]
                    val character = characterService.getCharacter(characterId) ?: return@mapNotNull null
                    val wallet = RPKWallet(
                            character,
                            currency,
                            result[RPKIT_WALLET.BALANCE]
                    )
                    cacheInsert(wallet)
                    return@mapNotNull wallet
                }
    }

    fun delete(entity: RPKWallet) {
        database.create
                .deleteFrom(RPKIT_WALLET)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_WALLET.CURRENCY_ID.eq(entity.currency.id))
                .execute()
        cacheRemove(entity)
    }

    private fun cacheInsert(entity: RPKWallet) {
        if (cache == null) return
        val characterId = entity.character.id
        val currencyId = entity.currency.id
        if (characterId == null || currencyId == null) return
        val currencyWallets = cache[characterId] as? MutableMap<Int, RPKWallet> ?: mutableMapOf()
        currencyWallets[currencyId] = entity
        cache.put(characterId, currencyWallets)
    }

    private fun cacheRemove(entity: RPKWallet) {
        if (cache == null) return
        val characterId = entity.character.id
        val currencyId = entity.currency.id
        if (characterId == null || currencyId == null) return
        val currencyWallets = cache[characterId] as? MutableMap<Int, RPKWallet> ?: mutableMapOf()
        currencyWallets.remove(currencyId)
        cache.put(characterId, currencyWallets)
    }

}