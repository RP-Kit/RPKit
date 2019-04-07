package com.rpkit.locks.bukkit.listener

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import org.bukkit.Material.AIR
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent


class InventoryClickListener(private val plugin: RPKLocksBukkit): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title.equals("Keyring", ignoreCase = true)) {
            val currentItem = event.currentItem
            if (currentItem != null) {
                event.isCancelled = true
                val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                if (lockProvider.isKey(currentItem)) {
                    event.isCancelled = false
                }
                if (currentItem.type == AIR) {
                    event.isCancelled = false
                }
                if (event.isCancelled) {
                    event.whoClicked.sendMessage(plugin.messages["keyring-invalid-item"])
                }
            }
        }
    }

}