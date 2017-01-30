package com.rpkit.essentials.bukkit.listener

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent




class PlayerQuitListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
        plugin.server.onlinePlayers
                .map { player -> playerProvider.getPlayer(player) }
                .filter { player -> logMessageProvider.isLogMessagesEnabled(player) }
                .forEach { player ->
                    player.bukkitPlayer?.player?.sendMessage(event.quitMessage)
                }
        event.quitMessage = ""
    }
}