package com.rpkit.essentials.bukkit.listener

import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
        plugin.server.onlinePlayers
                .map { player -> minecraftProfileProvider.getMinecraftProfile(player) }
                .filterNotNull()
                .filter { minecraftProfile -> logMessageProvider.isLogMessagesEnabled(minecraftProfile) }
                .forEach { minecraftProfile ->
            minecraftProfile.sendMessage(event.joinMessage)
        }
        event.joinMessage = ""
        val dailyQuoteProvider = plugin.core.serviceManager.getServiceProvider(RPKDailyQuoteProvider::class)
        event.player.sendMessage(dailyQuoteProvider.getDailyQuote())
    }

}