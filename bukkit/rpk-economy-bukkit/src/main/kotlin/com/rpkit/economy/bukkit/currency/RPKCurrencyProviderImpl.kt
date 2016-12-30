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

package com.rpkit.economy.bukkit.currency

import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.database.table.RPKCurrencyTable

/**
 * Currency provider implementation.
 */
class RPKCurrencyProviderImpl(private val plugin: RPKEconomyBukkit): RPKCurrencyProvider {

    override fun getCurrency(id: Int): RPKCurrency? {
        return plugin.core.database.getTable(RPKCurrencyTable::class)[id]
    }

    override fun getCurrency(name: String): RPKCurrency? {
        return plugin.core.database.getTable(RPKCurrencyTable::class).get(name)
    }

    override val currencies: Collection<RPKCurrency>
        get() = plugin.core.database.getTable(RPKCurrencyTable::class).getAll()

    override fun addCurrency(currency: RPKCurrency) {
        plugin.core.database.getTable(RPKCurrencyTable::class).insert(currency)
    }

    override fun removeCurrency(currency: RPKCurrency) {
        plugin.core.database.getTable(RPKCurrencyTable::class).delete(currency)
    }

    override val defaultCurrency: RPKCurrency?
        get() {
            val currencyName = plugin.config.getString("currency.default")
            if (currencyName != null)
                return getCurrency(currencyName)
            else
                return null
        }


}
