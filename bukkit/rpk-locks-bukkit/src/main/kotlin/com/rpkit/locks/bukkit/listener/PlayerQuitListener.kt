package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.keyring.RPKKeyringService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val keyringService = Services[RPKKeyringService::class.java] ?: return

        minecraftProfileService.getMinecraftProfile(event.player).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) return@getMinecraftProfile
            characterService.getActiveCharacter(minecraftProfile).thenAccept getActiveCharacter@{ character ->
                if (character == null) return@getActiveCharacter
                keyringService.unloadKeyring(character)
            }
        }
    }

}