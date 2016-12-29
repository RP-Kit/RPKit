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

package com.seventh_root.elysium.economy.bukkit.character

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.field.HideableCharacterCardField
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.database.table.MoneyHiddenTable
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider

/**
 * Character card field for money.
 * Shows money for all currencies, separated by ", "
 */
class MoneyField(val plugin: ElysiumEconomyBukkit): HideableCharacterCardField {

    override val name = "money"
    override fun get(character: ElysiumCharacter): String {
        if (isHidden(character)) {
            return "[HIDDEN]"
        } else {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
            return currencyProvider.currencies
                    .map { currency ->
                        val balance = economyProvider.getBalance(character, currency)
                        "$balance ${if (balance == 1) currency.nameSingular else currency.namePlural}"
                    }
                    .joinToString(", ")
        }
    }
    override fun isHidden(character: ElysiumCharacter): Boolean {
        return plugin.core.database.getTable(MoneyHiddenTable::class).get(character) != null
    }
    override fun setHidden(character: ElysiumCharacter, hidden: Boolean) {
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
