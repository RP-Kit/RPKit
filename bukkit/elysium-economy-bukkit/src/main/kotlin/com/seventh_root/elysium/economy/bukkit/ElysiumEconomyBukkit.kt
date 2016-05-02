package com.seventh_root.elysium.economy.bukkit

import com.seventh_root.elysium.api.economy.CurrencyProvider
import com.seventh_root.elysium.api.economy.EconomyProvider
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.command.currency.CurrencyCommand
import com.seventh_root.elysium.economy.bukkit.command.money.MoneyCommand
import com.seventh_root.elysium.economy.bukkit.command.money.MoneyPayCommand
import com.seventh_root.elysium.economy.bukkit.command.money.MoneyWalletCommand
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrency
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.database.table.BukkitCurrencyTable
import com.seventh_root.elysium.economy.bukkit.database.table.BukkitWalletTable
import com.seventh_root.elysium.economy.bukkit.economy.BukkitEconomyProvider
import com.seventh_root.elysium.economy.bukkit.listener.InventoryCloseListener


class ElysiumEconomyBukkit : ElysiumBukkitPlugin() {

    private var currencyProvider: CurrencyProvider<BukkitCurrency>? = null
    private var economyProvider: EconomyProvider? = null
    override var serviceProviders: Array<ServiceProvider>? = null

    override fun onEnable() {
        saveDefaultConfig()
        currencyProvider = BukkitCurrencyProvider(this)
        economyProvider = BukkitEconomyProvider(this)
        serviceProviders = arrayOf(
                currencyProvider as CurrencyProvider,
                economyProvider as EconomyProvider
        )
    }

    override fun registerCommands() {
        getCommand("money").executor = MoneyCommand(this)
        getCommand("pay").executor = MoneyPayCommand(this)
        getCommand("wallet").executor = MoneyWalletCommand(this)
        getCommand("currency").executor = CurrencyCommand(this)
    }

    override fun registerListeners() {
        registerListeners(InventoryCloseListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(BukkitCurrencyTable(database, this))
        database.addTable(BukkitWalletTable(database, this))
    }
}