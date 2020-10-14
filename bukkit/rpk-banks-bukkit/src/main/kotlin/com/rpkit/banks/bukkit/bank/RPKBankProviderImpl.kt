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

package com.rpkit.banks.bukkit.bank

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.database.table.RPKBankTable
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.economy.bukkit.exception.NegativeBalanceException

/**
 * Bank service implementation.
 */
class RPKBankServiceImpl(override val plugin: RPKBanksBukkit) : RPKBankService {

    override fun getBalance(character: RPKCharacter, currency: RPKCurrency): Int {
        return plugin.database.getTable(RPKBankTable::class)[character, currency]?.balance ?: 0
    }

    override fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val bankTable = plugin.database.getTable(RPKBankTable::class)
        var bank = bankTable[character, currency]
        if (bank != null) {
            bank.balance = amount
            bankTable.update(bank)
        } else {
            bank = RPKBank(
                    character,
                    currency,
                    amount
            )
            bankTable.insert(bank)
        }
    }

    override fun deposit(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        val economyService = Services[RPKEconomyService::class] ?: return
        if (economyService.getBalance(character, currency) >= amount) {
            economyService.setBalance(character, currency, economyService.getBalance(character, currency) - amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

    override fun withdraw(character: RPKCharacter, currency: RPKCurrency, amount: Int) {
        val economyService = Services[RPKEconomyService::class] ?: return
        if (getBalance(character, currency) >= amount) {
            economyService.setBalance(character, currency, economyService.getBalance(character, currency) + amount)
            setBalance(character, currency, getBalance(character, currency) - amount)
        }
    }

    override fun getRichestCharacters(currency: RPKCurrency, amount: Int): List<RPKCharacter> {
        return plugin.database.getTable(RPKBankTable::class)
                .getTop(amount, currency)
                .map(RPKBank::character)
    }

}