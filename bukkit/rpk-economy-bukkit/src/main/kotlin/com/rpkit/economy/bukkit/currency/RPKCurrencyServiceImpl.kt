/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.economy.bukkit.event.currency.RPKBukkitCurrencyCreateEvent
import com.rpkit.economy.bukkit.event.currency.RPKBukkitCurrencyDeleteEvent
import com.rpkit.economy.bukkit.event.currency.RPKBukkitCurrencyUpdateEvent

/**
 * Currency service implementation.
 */
class RPKCurrencyServiceImpl(override val plugin: RPKEconomyBukkit) : RPKCurrencyService {

    override fun getCurrency(id: RPKCurrencyId): RPKCurrency? {
        return plugin.database.getTable(RPKCurrencyTable::class.java)[id.value]
    }

    override fun getCurrency(name: RPKCurrencyName): RPKCurrency? {
        return plugin.database.getTable(RPKCurrencyTable::class.java)[name.value]
    }

    override val currencies: Collection<RPKCurrency>
        get() = plugin.database.getTable(RPKCurrencyTable::class.java).getAll()

    override fun addCurrency(currency: RPKCurrency) {
        val event = RPKBukkitCurrencyCreateEvent(currency)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKCurrencyTable::class.java).insert(event.currency)
    }

    override fun removeCurrency(currency: RPKCurrency) {
        val event = RPKBukkitCurrencyDeleteEvent(currency)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKCurrencyTable::class.java).delete(event.currency)
    }

    override fun updateCurrency(currency: RPKCurrency) {
        val event = RPKBukkitCurrencyUpdateEvent(currency)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKCurrencyTable::class.java).update(event.currency)
    }

    override val defaultCurrency: RPKCurrency?
        get() {
            val currencyName = plugin.config.getString("currency.default")
            return if (currencyName != null)
                getCurrency(RPKCurrencyName(currencyName))
            else
                null
        }


}
