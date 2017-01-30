package com.rpkit.essentials.bukkit.listener

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent


class PlayerTeleportListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val locationHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKLocationHistoryProvider::class)
        locationHistoryProvider.setPreviousLocation(playerProvider.getPlayer(event.player), event.from)
    }
}