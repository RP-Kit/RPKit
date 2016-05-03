package com.seventh_root.elysium.trade.bukkit

import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.trade.bukkit.listener.PlayerInteractListener
import com.seventh_root.elysium.trade.bukkit.listener.SignChangeListener


class ElysiumTradeBukkit: ElysiumBukkitPlugin() {

    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf()
    }

    override fun registerListeners() {
        registerListeners(SignChangeListener(this), PlayerInteractListener(this))
    }

}