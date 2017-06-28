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
            // Generate new token so account can be linked via web UI
            val minecraftProfileToken = RPKMinecraftProfileTokenImpl(
                    minecraftProfile = minecraftProfile,
                    token = UUID.randomUUID().toString()
            )
            minecraftProfileProvider.addMinecraftProfileToken(minecraftProfileToken)
        }
        if (minecraftProfile.profile == null) { // Either profile is new, or player has logged in before and still not linked account
            var minecraftProfileToken = minecraftProfileProvider.getMinecraftProfileToken(minecraftProfile)
            if (minecraftProfileToken == null) { // No token has been generated
                minecraftProfileToken = RPKMinecraftProfileTokenImpl(
                        minecraftProfile = minecraftProfile,
                        token = UUID.randomUUID().toString()
                )
                minecraftProfileProvider.addMinecraftProfileToken(minecraftProfileToken)
            }
            // Kick the player and notify of them of the token they need to use in the web UI
            event.kickMessage = plugin.messages["kick-no-profile", mapOf(
                    Pair("token", minecraftProfileToken.token)
            )]
            event.result = PlayerLoginEvent.Result.KICK_OTHER
        }
    }
}