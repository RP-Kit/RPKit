package com.rpkit.players.bukkit.listener

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent


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
        } else if (minecraftProfileProvider.getMinecraftProfileLinkRequests(minecraftProfile).isNotEmpty()) { // Minecraft profile has a link request, so skip and let them know on join.
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        var profile = minecraftProfile.profile
        if (profile == null) {
            profile = RPKProfileImpl(
                    event.player.name,
                    ""
            )
            profileProvider.addProfile(profile)
            minecraftProfile.profile = profile
            minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
        }
    }
}