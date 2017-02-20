package com.rpkit.locks.bukkit.listener

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent


class CraftItemListener(private val plugin: RPKLocksBukkit): Listener {

    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        for (item in event.inventory.contents) {
            if (item != null) {
                val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                if (lockProvider.isKey(item)) {
                    event.isCancelled = true
                    event.whoClicked.sendMessage(plugin.messages["crafting-no-keys"])
                }
            }
        }
    }
}