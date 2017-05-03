package com.rpkit.essentials.bukkit.listener

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent


class PlayerTeleportListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val locationHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKLocationHistoryProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
        if (minecraftProfile != null) {
            locationHistoryProvider.setPreviousLocation(minecraftProfile, event.from)
        }
    }
}