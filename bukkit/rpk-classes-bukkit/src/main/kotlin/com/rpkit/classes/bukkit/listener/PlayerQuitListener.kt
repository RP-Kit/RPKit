package com.rpkit.classes.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val classService = Services[RPKClassService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) return@getMinecraftProfile
            characterService.getActiveCharacter(minecraftProfile).thenAccept getCharacter@{ character ->
                if (character == null) return@getCharacter
                classService.getClass(character).thenAccept getClass@{ `class` ->
                    if (`class` == null) return@getClass
                    // If a player relogs quickly, then by the time the data has been retrieved, the player is sometimes back
                    // online. We only want to unload data if the player is offline.
                    if (!minecraftProfile.isOnline) {
                        classService.unloadExperience(character, `class`)
                        classService.unloadClass(character)
                    }
                }
            }
        }
    }

}