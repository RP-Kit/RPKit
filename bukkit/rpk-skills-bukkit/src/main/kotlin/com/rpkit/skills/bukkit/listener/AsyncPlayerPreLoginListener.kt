package com.rpkit.skills.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.skills.bukkit.skills.RPKSkillService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.concurrent.CompletableFuture

class AsyncPlayerPreLoginListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val skillService = Services[RPKSkillService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join() ?: return
        val character = characterService.getActiveCharacter(minecraftProfile).join() ?: return
        CompletableFuture.allOf(
            skillService.loadSkillBindings(character),
            skillService.loadSkillCooldowns(character)
        ).join()
    }

}