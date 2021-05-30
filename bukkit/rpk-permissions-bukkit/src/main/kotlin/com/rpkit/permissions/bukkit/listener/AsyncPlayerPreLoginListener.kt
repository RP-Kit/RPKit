package com.rpkit.permissions.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val groupService = Services[RPKGroupService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join() ?: return
        val profile = minecraftProfile.profile
        if (profile is RPKProfile) {
            groupService.loadGroups(profile).join()
        }

        val characterService = Services[RPKCharacterService::class.java] ?: return
        val character = characterService.getActiveCharacter(minecraftProfile).join() ?: return
        groupService.loadGroups(character).join()
    }

}