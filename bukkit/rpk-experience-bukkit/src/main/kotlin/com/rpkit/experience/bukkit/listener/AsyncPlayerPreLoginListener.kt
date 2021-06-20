package com.rpkit.experience.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join() ?: return
        val character = characterService.getActiveCharacter(minecraftProfile).join() ?: return
        experienceService.loadExperience(character).join()
    }

}