package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.economy.bukkit.currency.CurrencyProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.database.table.BukkitCurrencyTable

class BukkitCurrencyProvider(private val plugin: ElysiumEconomyBukkit) : CurrencyProvider<BukkitCurrency> {

    override fun getCurrency(id: Int): BukkitCurrency? {
        return plugin.core.database.getTable(BukkitCurrency::class.java)?.get(id)
    }

    override fun getCurrency(name: String): BukkitCurrency? {
        return (plugin.core.database.getTable(BukkitCurrency::class.java) as BukkitCurrencyTable).get(name)
    }

    override val currencies: Collection<BukkitCurrency>
        get() = (plugin.core.database.getTable(BukkitCurrency::class.java) as BukkitCurrencyTable).getAll()

    override fun addCurrency(currency: BukkitCurrency) {
        plugin.core.database.getTable(BukkitCurrency::class.java)?.insert(currency)
    }

    override fun removeCurrency(currency: BukkitCurrency) {
        plugin.core.database.getTable(BukkitCurrency::class.java)?.delete(currency)
    }

    override val defaultCurrency: BukkitCurrency?
        get() {
            val currencyName = plugin.config.getString("currency.default")
            if (currencyName != null)
                return getCurrency(currencyName)
            else
                return null
        }


}
