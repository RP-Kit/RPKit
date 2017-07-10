package com.rpkit.players.bukkit.listener

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileTokenImpl
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.util.*


class PlayerLoginListener(private val plugin: RPKPlayersBukkit): Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        var minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
        if (minecraftProfile == null) { // Player hasn't logged in while profile generation is active
            minecraftProfile = RPKMinecraftProfileImpl(
                    profile = null,
                    minecraftUUID = event.player.uniqueId
            )
            minecraftProfileProvider.addMinecraftProfile(minecraftProfile)
            // Generate new token so account can be linked
            val minecraftProfileToken = RPKMinecraftProfileTokenImpl(
                    minecraftProfile = minecraftProfile,
                    token = UUID.randomUUID().toString()
            )
            minecraftProfileProvider.addMinecraftProfileToken(minecraftProfileToken)
        }
    }
}