package com.seventh_root.elysium.shops.bukkit

import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.shops.bukkit.listener.BlockBreakListener
import com.seventh_root.elysium.shops.bukkit.listener.InventoryClickListener
import com.seventh_root.elysium.shops.bukkit.listener.PlayerInteractListener
import com.seventh_root.elysium.shops.bukkit.listener.SignChangeListener
import com.seventh_root.elysium.shops.bukkit.shopcount.BukkitShopCountProvider


class ElysiumShopsBukkit: ElysiumBukkitPlugin() {

    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        serviceProviders = arrayOf(
                BukkitShopCountProvider(this)
        )
    }

    override fun registerListeners() {
        registerListeners(
                SignChangeListener(this),
                BlockBreakListener(this),
                PlayerInteractListener(this),
                InventoryClickListener(this)
        )
    }

}