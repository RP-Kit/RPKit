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

package com.seventh_root.elysium.economy.bukkit.economy

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.database.table.ElysiumWalletTable
import com.seventh_root.elysium.economy.bukkit.exception.NegativeBalanceException
import com.seventh_root.elysium.economy.bukkit.wallet.ElysiumWallet


class ElysiumEconomyProvider(val plugin: ElysiumEconomyBukkit): ServiceProvider {

    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int {
        return (plugin.core.database.getTable(com.seventh_root.elysium.economy.bukkit.wallet.ElysiumWallet::class.java) as ElysiumWalletTable).get(character, currency).balance
    }

    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val wallet = (plugin.core.database.getTable(ElysiumWallet::class.java) as ElysiumWalletTable).get(character, currency)
        wallet.balance = amount
        plugin.core.database.getTable(ElysiumWallet::class.java)!!.update(wallet)
    }

    fun transfer(from: ElysiumCharacter, to: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        setBalance(from, currency, getBalance(from, currency) - amount)
        setBalance(to, currency, getBalance(to, currency) + amount)
    }

    fun getRichestCharacters(currency: ElysiumCurrency, amount: Int): List<ElysiumCharacter> {
        return (plugin.core.database.getTable(ElysiumWallet::class.java) as ElysiumWalletTable).getTop(amount, currency)
    }

}