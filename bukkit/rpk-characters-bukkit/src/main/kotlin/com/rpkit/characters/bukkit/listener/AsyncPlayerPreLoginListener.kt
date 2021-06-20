package com.rpkit.characters.bukkit.listener

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener(private val plugin: RPKCharactersBukkit) : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, plugin.messages.noMinecraftProfileService)
            return
        }
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join()
        if (minecraftProfile != null) {
            characterService.loadActiveCharacter(minecraftProfile).join()
        }
    }

}