package com.seventh_root.elysium.banks.bukkit

import com.seventh_root.elysium.banks.bukkit.bank.ElysiumBankProvider
import com.seventh_root.elysium.banks.bukkit.database.table.ElysiumBankTable
import com.seventh_root.elysium.banks.bukkit.listener.PlayerInteractListener
import com.seventh_root.elysium.banks.bukkit.listener.SignChangeListener
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider


class ElysiumBanksBukkit: ElysiumBukkitPlugin() {

    private lateinit var bankProvider: ElysiumBankProvider
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        bankProvider = ElysiumBankProvider(this)
        serviceProviders = arrayOf(
                bankProvider
        )
    }

    override fun registerListeners() {
        registerListeners(SignChangeListener(this), PlayerInteractListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(ElysiumBankTable(database, this))
    }

}