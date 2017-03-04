package com.rpkit.food.bukkit.listener

import com.rpkit.food.bukkit.RPKFoodBukkit
import com.rpkit.food.bukkit.expiry.RPKExpiryProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: RPKFoodBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val expiryProvider = plugin.core.serviceManager.getServiceProvider(RPKExpiryProvider::class)
        event.player.inventory.contents
                .filterNotNull()
                .filter { it.type.isEdible }
                .forEach { item ->
                    if (expiryProvider.getExpiry(item) == null) {
                        expiryProvider.setExpiry(item)
                    }
                }
    }

}
