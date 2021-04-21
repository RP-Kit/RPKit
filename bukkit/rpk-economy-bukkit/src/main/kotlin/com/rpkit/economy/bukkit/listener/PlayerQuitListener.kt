package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val economyService = Services[RPKEconomyService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val character = characterService.getActiveCharacter(minecraftProfile).thenAccept { character ->
            if (character == null) return@thenAccept
            economyService.unloadBalances(character)
        }
    }

}