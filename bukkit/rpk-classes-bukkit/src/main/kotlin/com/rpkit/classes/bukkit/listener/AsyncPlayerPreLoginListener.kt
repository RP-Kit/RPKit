package com.rpkit.classes.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val classService = Services[RPKClassService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join() ?: return
        val character = characterService.getActiveCharacter(minecraftProfile).join() ?: return
        val `class` = classService.loadClass(character).join()
        if (`class` != null) {
            classService.loadExperience(character, `class`).join()
        }
    }

}