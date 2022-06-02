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

package com.rpkit.economy.bukkit.economy

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.database.table.RPKWalletTable
import com.rpkit.economy.bukkit.event.economy.RPKBukkitBalanceChangeEvent
import com.rpkit.economy.bukkit.exception.NegativeBalanceException
import com.rpkit.economy.bukkit.wallet.RPKWallet
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

/**
 * Economy service implementation.
 */
class RPKEconomyServiceImpl(override val plugin: RPKEconomyBukkit) : RPKEconomyService {

    private val balance: MutableMap<Int, MutableMap<RPKCurrency, Int>> = ConcurrentHashMap()

    override fun getPreloadedBalance(character: RPKCharacter, currency: RPKCurrency): Int? {
        val characterId = character.id?.value ?: return null
        return balance[characterId]?.get(currency)
    }

    override fun loadBalances(character: RPKCharacter): CompletableFuture<Void> {
        val characterId = character.id?.value ?: return CompletableFuture.completedFuture(null)
        val currencyService = Services[RPKCurrencyService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val characterBalances = balance[characterId] ?: ConcurrentHashMap()
            currencyService.currencies.forEach { currency ->
                characterBalances[currency] = getBalance(character, currency).join()
            }
            balance[characterId] = characterBalances
        }
    }

    override fun unloadBalances(character: RPKCharacter) {
        val characterId = character.id?.value ?: return
        balance.remove(characterId)
    }

    override fun getBalance(character: RPKCharacter, currency: RPKCurrency): CompletableFuture<Int> {
        val preloadedBalance = getPreloadedBalance(character, currency)
        if (preloadedBalance != null) return CompletableFuture.completedFuture(preloadedBalance)
        return plugin.database.getTable(RPKWalletTable::class.java).get(character, currency).thenApply { it?.balance ?: currency.defaultAmount }
    }

    override fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitBalanceChangeEvent(character, currency, getBalance(character, currency).join(), amount, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            if (event.newBalance < 0) throw NegativeBalanceException()
            val walletTable = plugin.database.getTable(RPKWalletTable::class.java)
            walletTable.get(event.character, event.currency).thenAcceptAsync { fetchedWallet ->
                val wallet = fetchedWallet ?: RPKWallet(event.character, event.currency, event.newBalance).also { walletTable.insert(it).join() }
                wallet.balance = event.newBalance
                walletTable.update(wallet).join()
                val characterId = event.character.id?.value
                if (characterId != null) {
                    val characterBalances = balance[characterId]
                    if (characterBalances != null) {
                        characterBalances[currency] = amount
                    }
                }
            }.join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set balance", exception)
            throw exception
        }
    }

    override fun transfer(from: RPKCharacter, to: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            setBalance(from, currency, getBalance(from, currency).join() - amount).join()
            setBalance(to, currency, getBalance(to, currency).join() + amount).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to transfer", exception)
            throw exception
        }
    }

    override fun getRichestCharacters(currency: RPKCurrency, amount: Int): CompletableFuture<List<RPKCharacter>> {
        return plugin.database.getTable(RPKWalletTable::class.java).getTop(amount, currency).thenApply { wallets ->
            wallets.map(RPKWallet::character)
        }
    }

}