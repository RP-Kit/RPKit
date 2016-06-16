package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.core.service.ServiceProvider


interface CurrencyProvider<T: ElysiumCurrency> : ServiceProvider {

    fun getCurrency(id: Int): T?
    fun getCurrency(name: String): T?
    val currencies: Collection<T>
    fun addCurrency(currency: T)
    fun removeCurrency(currency: T)
    val defaultCurrency: T?

}