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

package com.rpkit.economy.bukkit.economy

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.database.table.RPKWalletTable
import com.rpkit.economy.bukkit.exception.NegativeBalanceException

/**
 * Economy provider implementation.
 */
class RPKEconomyProviderImpl(val plugin: RPKEconomyBukkit): RPKEconomyProvider {

    override fun getBalance(character: RPKCharacter, currency: RPKCurrency): Int {
        return plugin.core.database.getTable(RPKWalletTable::class).get(character, currency).balance
    }

    override fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val wallet = plugin.core.database.getTable(RPKWalletTable::class).get(character, currency)
        wallet.balance = amount
        plugin.core.database.getTable(RPKWalletTable::class).update(wallet)
    }

    override fun transfer(from: RPKCharacter, to: RPKCharacter, currency: RPKCurrency, amount: Int) {
        setBalance(from, currency, getBalance(from, currency) - amount)
        setBalance(to, currency, getBalance(to, currency) + amount)
    }

    override fun getRichestCharacters(currency: RPKCurrency, amount: Int): List<RPKCharacter> {
        return plugin.core.database.getTable(RPKWalletTable::class).getTop(amount, currency)
    }

}