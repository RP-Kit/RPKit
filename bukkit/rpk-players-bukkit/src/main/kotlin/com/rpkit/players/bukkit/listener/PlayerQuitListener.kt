package com.rpkit.players.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture

class PlayerQuitListener(private val plugin: RPKPlayersBukkit) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val profileService = Services[RPKProfileService::class.java] ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept { minecraftProfile ->
            if (minecraftProfile == null) return@thenAccept
            val profile = minecraftProfile.profile
            plugin.server.scheduler.runTask(plugin, Runnable {
                val minecraftProfileFutures = plugin.server.onlinePlayers.map { bukkitPlayer ->
                    minecraftProfileService.getMinecraftProfile(bukkitPlayer)
                }
                if (profile is RPKProfile) {
                    CompletableFuture.runAsync {
                        CompletableFuture.allOf(*minecraftProfileFutures.toTypedArray()).join()
                        val minecraftProfiles =
                            minecraftProfileFutures.mapNotNull(CompletableFuture<RPKMinecraftProfile?>::join)
                                .filter { it != minecraftProfile }
                        if (minecraftProfiles.none {
                            val otherProfile = it.profile
                            return@none otherProfile is RPKProfile && otherProfile.id?.value == profile.id?.value
                        }) {
                            profileService.unloadProfile(profile)
                        }
                    }
                }
            })
            minecraftProfileService.unloadMinecraftProfile(minecraftProfile)
        }
    }

}