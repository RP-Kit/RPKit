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

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider

class MoneyField(val plugin: ElysiumEconomyBukkit): CharacterCardField {

    override val name = "money"
    override fun get(character: ElysiumCharacter): String {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        return currencyProvider.currencies
                .map { currency ->
                    val balance = economyProvider.getBalance(character, currency)
                    "${balance.toString()} ${if (balance == 1) currency.nameSingular else currency.namePlural}"
                }
                .joinToString(", ")
    }

}
