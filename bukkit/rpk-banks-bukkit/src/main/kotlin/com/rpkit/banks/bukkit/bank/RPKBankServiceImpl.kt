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

package com.rpkit.banks.bukkit.bank

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.database.table.RPKBankTable
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.economy.bukkit.exception.NegativeBalanceException
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Bank service implementation.
 */
class RPKBankServiceImpl(override val plugin: RPKBanksBukkit) : RPKBankService {

    override fun getBalance(character: RPKCharacter, currency: RPKCurrency): CompletableFuture<Int> {
        return plugin.database.getTable(RPKBankTable::class.java)[character, currency]
            .thenApply { bank -> bank?.balance ?: 0 }
    }

    override fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (amount < 0) throw NegativeBalanceException()
            val bankTable = plugin.database.getTable(RPKBankTable::class.java)
            bankTable[character, currency].thenAccept { bank ->
                if (bank != null) {
                    bank.balance = amount
                    bankTable.update(bank).join()
                } else {
                    bankTable.insert(
                        RPKBank(
                            character,
                            currency,
                            amount
                        )
                    ).join()
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set bank balance", exception)
            throw exception
        }
    }

    override fun deposit(character: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void> {
        val economyService = Services[RPKEconomyService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val walletBalance = economyService.getBalance(character, currency).join()
            if (walletBalance >= amount) {
                economyService.setBalance(character, currency, walletBalance - amount).join()
                val bankBalance = getBalance(character, currency).join()
                setBalance(character, currency, bankBalance + amount).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to deposit to bank", exception)
            throw exception
        }
    }

    override fun withdraw(character: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void> {
        val economyService = Services[RPKEconomyService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val bankBalance = getBalance(character, currency).join()
            if (bankBalance >= amount) {
                val walletBalance = economyService.getBalance(character, currency).join()
                economyService.setBalance(character, currency, walletBalance + amount).join()
                setBalance(character, currency, bankBalance - amount)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to withdraw from bank", exception)
            throw exception
        }
    }

    override fun getRichestCharacters(currency: RPKCurrency, amount: Int): CompletableFuture<List<RPKCharacter>> {
        return plugin.database.getTable(RPKBankTable::class.java)
            .getTop(amount, currency)
            .thenApply { banks -> banks.map(RPKBank::character) }
    }

}