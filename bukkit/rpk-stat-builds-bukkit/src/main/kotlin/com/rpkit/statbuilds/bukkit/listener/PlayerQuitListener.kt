package com.rpkit.statbuilds.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val statBuildService = Services[RPKStatBuildService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) return@getMinecraftProfile
            characterService.getActiveCharacter(minecraftProfile).thenAccept getCharacter@{ character ->
                if (character == null) return@getCharacter
                statBuildService.unloadStatPoints(character)
            }
        }
    }

}