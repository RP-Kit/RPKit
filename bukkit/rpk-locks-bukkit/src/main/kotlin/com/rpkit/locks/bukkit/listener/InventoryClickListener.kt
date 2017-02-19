package com.rpkit.locks.bukkit.listener

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import org.bukkit.ChatColor
import org.bukkit.Material.AIR
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent


class InventoryClickListener(private val plugin: RPKLocksBukkit): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.title.equals("Keyring", ignoreCase = true)) {
            if (event.currentItem != null) {
                event.isCancelled = true
                val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                if (lockProvider.isKey(event.currentItem)) {
                    event.isCancelled = false
                }
                if (event.currentItem.type == AIR) {
                    event.isCancelled = false
                }
                if (event.isCancelled) {
                    event.whoClicked.sendMessage(plugin.core.messages["keyring-invalid-item"])
                }
            }
        }
    }

}