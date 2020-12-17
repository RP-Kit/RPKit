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

package com.rpkit.economy.bukkit.economy

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.database.table.RPKWalletTable
import com.rpkit.economy.bukkit.event.economy.RPKBukkitBalanceChangeEvent
import com.rpkit.economy.bukkit.exception.NegativeBalanceException
import com.rpkit.economy.bukkit.wallet.RPKWallet

/**
 * Economy service implementation.
 */
class RPKEconomyServiceImpl(override val plugin: RPKEconomyBukkit) : RPKEconomyService {

    override fun getBalance(character: RPKCharacter, currency: RPKCurrency): Int {
        return plugin.database.getTable(RPKWalletTable::class.java).get(character, currency)?.balance ?: currency.defaultAmount
    }

    override fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        val event = RPKBukkitBalanceChangeEvent(character, currency, getBalance(character, currency), amount)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        if (event.newBalance < 0) throw NegativeBalanceException()
        val walletTable = plugin.database.getTable(RPKWalletTable::class.java)
        val wallet = walletTable.get(event.character, event.currency)
                ?: RPKWallet(event.character, event.currency, event.newBalance).also { walletTable.insert(it) }
        wallet.balance = event.newBalance
        walletTable.update(wallet)
    }

    override fun transfer(from: RPKCharacter, to: RPKCharacter, currency: RPKCurrency, amount: Int) {
        setBalance(from, currency, getBalance(from, currency) - amount)
        setBalance(to, currency, getBalance(to, currency) + amount)
    }

    override fun getRichestCharacters(currency: RPKCurrency, amount: Int): List<RPKCharacter> {
        return plugin.database.getTable(RPKWalletTable::class.java).getTop(amount, currency).map(RPKWallet::character)
    }

}