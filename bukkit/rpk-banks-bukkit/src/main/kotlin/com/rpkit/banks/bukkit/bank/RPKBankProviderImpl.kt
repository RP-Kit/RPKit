/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.economy.bukkit.exception.NegativeBalanceException

/**
 * Bank provider implementation.
 */
class RPKBankProviderImpl(private val plugin: RPKBanksBukkit): RPKBankProvider {

    override fun getBalance(character: RPKCharacter, currency: RPKCurrency): Int {
        return plugin.core.database.getTable(RPKBankTable::class).get(character, currency).balance
    }

    override fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val bank = plugin.core.database.getTable(RPKBankTable::class).get(character, currency)
        bank.balance = amount
        plugin.core.database.getTable(RPKBankTable::class).update(bank)
    }

    override fun deposit(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        if (economyProvider.getBalance(character, currency) >= amount) {
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

    override fun withdraw(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        if (getBalance(character, currency) >= amount) {
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

}