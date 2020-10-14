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

package com.rpkit.economy.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.HideableCharacterCardField
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.database.table.RPKMoneyHiddenTable
import com.rpkit.economy.bukkit.economy.RPKEconomyService

/**
 * Character card field for money.
 * Shows money for all currencies, separated by ", "
 */
class MoneyField(val plugin: RPKEconomyBukkit) : HideableCharacterCardField {

    override val name = "money"
    override fun get(character: RPKCharacter): String {
        return if (isHidden(character)) {
            "[HIDDEN]"
        } else {
            val economyService = Services[RPKEconomyService::class] ?: return plugin.messages["no-economy-service"]
            val currencyService = Services[RPKCurrencyService::class] ?: return plugin.messages["no-currency-service"]
            currencyService.currencies
                    .joinToString(", ") { currency ->
                        val balance = economyService.getBalance(character, currency)
                        "$balance ${if (balance == 1) currency.nameSingular else currency.namePlural}"
                    }
        }
    }

    override fun isHidden(character: RPKCharacter): Boolean {
        return plugin.database.getTable(RPKMoneyHiddenTable::class).get(character) != null
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean) {
        val moneyHiddenTable = plugin.database.getTable(RPKMoneyHiddenTable::class)
        if (hidden) {
            if (moneyHiddenTable.get(character) == null) {
                moneyHiddenTable.insert(RPKMoneyHidden(character = character))
            }
        } else {
            val moneyHidden = moneyHiddenTable.get(character)
            if (moneyHidden != null) {
                moneyHiddenTable.delete(moneyHidden)
            }
        }
    }

}
