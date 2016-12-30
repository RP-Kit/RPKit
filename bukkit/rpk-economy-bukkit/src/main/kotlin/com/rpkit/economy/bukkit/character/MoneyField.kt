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

package com.rpkit.economy.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.HideableCharacterCardField
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.database.table.MoneyHiddenTable
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider

/**
 * Character card field for money.
 * Shows money for all currencies, separated by ", "
 */
class MoneyField(val plugin: RPKEconomyBukkit): HideableCharacterCardField {

    override val name = "money"
    override fun get(character: RPKCharacter): String {
        if (isHidden(character)) {
            return "[HIDDEN]"
        } else {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            return currencyProvider.currencies
                    .map { currency ->
                        val balance = economyProvider.getBalance(character, currency)
                        "$balance ${if (balance == 1) currency.nameSingular else currency.namePlural}"
                    }
                    .joinToString(", ")
        }
    }
    override fun isHidden(character: RPKCharacter): Boolean {
        return plugin.core.database.getTable(MoneyHiddenTable::class).get(character) != null
    }
    override fun setHidden(character: RPKCharacter, hidden: Boolean) {
        val moneyHiddenTable = plugin.core.database.getTable(MoneyHiddenTable::class)
        if (hidden) {
            if (moneyHiddenTable.get(character) == null) {
                moneyHiddenTable.insert(MoneyHidden(character = character))
            }
        } else {
            val moneyHidden = moneyHiddenTable.get(character)
            if (moneyHidden != null) {
                moneyHiddenTable.delete(moneyHidden)
            }
        }
    }

}
