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

package com.rpkit.economy.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.database.create
import com.rpkit.economy.bukkit.database.jooq.Tables.RPKIT_WALLET
import com.rpkit.economy.bukkit.wallet.RPKWallet
import java.util.concurrent.CompletableFuture

/**
 * Represents the wallet table.
 */
class RPKWalletTable(
        private val database: Database,
        plugin: RPKEconomyBukkit
) : Table {

    private data class CharacterCurrencyCacheKey(
        val characterId: Int,
        val currencyName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_wallet.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-economy-bukkit.rpkit_wallet.character_id",
            CharacterCurrencyCacheKey::class.java,
            RPKWallet::class.java,
            plugin.config.getLong("caching.rpkit_wallet.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKWallet) {
        val characterId = entity.character.id ?: return
        val currencyName = entity.currency.name
        database.create
                .insertInto(
                        RPKIT_WALLET,
                        RPKIT_WALLET.CHARACTER_ID,
                        RPKIT_WALLET.CURRENCY_NAME,
                        RPKIT_WALLET.BALANCE
                )
                .values(
                    characterId.value,
                    currencyName.value,
                    entity.balance
                )
                .execute()
        cache?.set(CharacterCurrencyCacheKey(characterId.value, currencyName.value), entity)
    }

    fun update(entity: RPKWallet) {
        val characterId = entity.character.id ?: return
        val currencyName = entity.currency.name
        database.create
                .update(RPKIT_WALLET)
                .set(RPKIT_WALLET.BALANCE, entity.balance)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_WALLET.CURRENCY_NAME.eq(currencyName.value))
                .execute()
        cache?.set(CharacterCurrencyCacheKey(characterId.value, currencyName.value), entity)
    }

    fun get(character: RPKCharacter, currency: RPKCurrency): RPKWallet? {
        val characterId = character.id ?: return null
        val currencyName = currency.name
        val cacheKey = CharacterCurrencyCacheKey(characterId.value, currencyName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return cache[cacheKey]
        }
        val result = database.create
                .select(RPKIT_WALLET.BALANCE)
                .from(RPKIT_WALLET)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_WALLET.CURRENCY_NAME.eq(currencyName.value))
                .fetchOne() ?: return null
        val wallet = RPKWallet(
                character,
                currency,
                result[RPKIT_WALLET.BALANCE]
        )
        cache?.set(cacheKey, wallet)
        return wallet
    }

    fun getTop(amount: Int = 5, currency: RPKCurrency): CompletableFuture<List<RPKWallet>> {
        return CompletableFuture.supplyAsync {
            val currencyName = currency.name
            val results = database.create
                .select(
                    RPKIT_WALLET.CHARACTER_ID,
                    RPKIT_WALLET.BALANCE
                )
                .from(RPKIT_WALLET)
                .where(RPKIT_WALLET.CURRENCY_NAME.eq(currencyName.value))
                .orderBy(RPKIT_WALLET.BALANCE.desc())
                .limit(amount)
                .fetch()
            val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync emptyList()
            return@supplyAsync results
                .mapNotNull { result ->
                    val characterId = result[RPKIT_WALLET.CHARACTER_ID]
                    val character =
                        characterService.getCharacter(RPKCharacterId(characterId)).join() ?: return@mapNotNull null
                    val wallet = RPKWallet(
                        character,
                        currency,
                        result[RPKIT_WALLET.BALANCE]
                    )
                    cache?.set(CharacterCurrencyCacheKey(characterId, currencyName.value), wallet)
                    return@mapNotNull wallet
                }
        }
    }

    fun delete(entity: RPKWallet) {
        val characterId = entity.character.id ?: return
        val currencyName = entity.currency.name
        database.create
                .deleteFrom(RPKIT_WALLET)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_WALLET.CURRENCY_NAME.eq(currencyName.value))
                .execute()
        cache?.remove(CharacterCurrencyCacheKey(characterId.value, currencyName.value))
    }

}