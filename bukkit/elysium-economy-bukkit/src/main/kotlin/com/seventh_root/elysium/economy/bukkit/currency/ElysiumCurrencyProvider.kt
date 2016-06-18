package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.database.table.ElysiumCurrencyTable

class ElysiumCurrencyProvider(private val plugin: ElysiumEconomyBukkit): ServiceProvider {

    fun getCurrency(id: Int): ElysiumCurrency? {
        return plugin.core.database.getTable(ElysiumCurrency::class.java)?.get(id)
    }

    fun getCurrency(name: String): ElysiumCurrency? {
        return (plugin.core.database.getTable(ElysiumCurrency::class.java) as ElysiumCurrencyTable).get(name)
    }

    val currencies: Collection<ElysiumCurrency>
        get() = (plugin.core.database.getTable(ElysiumCurrency::class.java) as ElysiumCurrencyTable).getAll()

    fun addCurrency(currency: ElysiumCurrency) {
        plugin.core.database.getTable(ElysiumCurrency::class.java)?.insert(currency)
    }

    fun removeCurrency(currency: ElysiumCurrency) {
        plugin.core.database.getTable(ElysiumCurrency::class.java)?.delete(currency)
    }

    val defaultCurrency: ElysiumCurrency?
        get() {
            val currencyName = plugin.config.getString("currency.default")
            if (currencyName != null)
                return getCurrency(currencyName)
            else
                return null
        }


}
