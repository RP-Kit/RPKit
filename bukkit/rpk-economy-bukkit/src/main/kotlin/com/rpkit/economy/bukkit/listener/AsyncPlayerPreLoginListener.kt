package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val economyService = Services[RPKEconomyService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join() ?: return
        val character = characterService.getActiveCharacter(minecraftProfile).join() ?: return
        economyService.loadBalances(character).join()
    }

}