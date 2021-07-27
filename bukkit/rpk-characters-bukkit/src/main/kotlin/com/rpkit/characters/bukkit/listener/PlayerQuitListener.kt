package com.rpkit.characters.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler(priority = MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept { minecraftProfile ->
            if (minecraftProfile == null) return@thenAccept
            // If a player relogs quickly, then by the time the data has been retrieved, the player is sometimes back
            // online. We only want to unload data if the player is offline.
            if (!minecraftProfile.isOnline) {
                characterService.unloadActiveCharacter(minecraftProfile)
            }
        }

    }

}