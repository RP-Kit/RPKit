package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val professionService = Services[RPKProfessionService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) return@getMinecraftProfile
            characterService.getActiveCharacter(minecraftProfile).thenAccept getCharacter@{ character ->
                if (character == null) return@getCharacter
                professionService.getProfessions(character).thenAccept { professions ->
                    // If a player relogs quickly, then by the time the data has been retrieved, the player is sometimes back
                    // online. We only want to unload data if the player is offline.
                    if (!minecraftProfile.isOnline) {
                        professions.forEach { profession ->
                            professionService.unloadProfessionExperience(character, profession)
                        }
                        professionService.unloadProfessions(character)
                    }
                }
            }
        }
    }

}