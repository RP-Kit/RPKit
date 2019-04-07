package com.rpkit.essentials.bukkit.listener

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent




class PlayerQuitListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
        plugin.server.onlinePlayers
                .mapNotNull { player -> minecraftProfileProvider.getMinecraftProfile(player) }
                .filter { minecraftProfile -> logMessageProvider.isLogMessagesEnabled(minecraftProfile) }
                .forEach { minecraftProfile ->
                    minecraftProfile.sendMessage(event.quitMessage)
                }
        event.quitMessage = ""
    }
}