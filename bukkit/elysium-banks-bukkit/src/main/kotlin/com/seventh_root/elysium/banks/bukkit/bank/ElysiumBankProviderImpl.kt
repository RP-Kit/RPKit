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

package com.seventh_root.elysium.banks.bukkit.bank

import com.seventh_root.elysium.banks.bukkit.ElysiumBanksBukkit
import com.seventh_root.elysium.banks.bukkit.database.table.ElysiumBankTable
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.economy.bukkit.exception.NegativeBalanceException

/**
 * Bank provider implementation.
 */
class ElysiumBankProviderImpl(private val plugin: ElysiumBanksBukkit): ElysiumBankProvider {

    override fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int {
        return plugin.core.database.getTable(ElysiumBankTable::class).get(character, currency).balance
    }

    override fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val bank = plugin.core.database.getTable(ElysiumBankTable::class).get(character, currency)
        bank.balance = amount
        plugin.core.database.getTable(ElysiumBankTable::class).update(bank)
    }

    override fun deposit(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        if (economyProvider.getBalance(character, currency) >= amount) {
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

    override fun withdraw(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        if (getBalance(character, currency) >= amount) {
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

}