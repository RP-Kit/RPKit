package com.seventh_root.elysium.core.bukkit

import com.seventh_root.elysium.core.ElysiumCore
import com.seventh_root.elysium.core.bukkit.listener.PluginEnableListener
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider

import java.sql.SQLException

class ElysiumCoreBukkit : ElysiumBukkitPlugin() {

    override var serviceProviders: Array<ServiceProvider>? = null

    override fun onEnable() {
        saveDefaultConfig()
        core = ElysiumCore(logger, Database(config.getString("database.url"), config.getString("database.username"), config.getString("database.password")))
        try {
            createTables(core!!.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        serviceProviders = arrayOf<ServiceProvider>()
        registerServiceProviders(this)
        registerCommands()
        registerListeners()
    }

    override fun registerListeners() {
        registerListeners(PluginEnableListener(this))
    }

    fun registerServiceProviders(plugin: ElysiumBukkitPlugin) {
        for (provider in plugin.serviceProviders!!) {
            core!!.serviceManager.registerServiceProvider(provider)
        }
    }

    fun initializePlugin(elysiumBukkitPlugin: ElysiumBukkitPlugin) {
        elysiumBukkitPlugin.core = core
        try {
            elysiumBukkitPlugin.createTables(core!!.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        registerServiceProviders(elysiumBukkitPlugin)
        elysiumBukkitPlugin.registerCommands()
        elysiumBukkitPlugin.registerListeners()
    }

}
