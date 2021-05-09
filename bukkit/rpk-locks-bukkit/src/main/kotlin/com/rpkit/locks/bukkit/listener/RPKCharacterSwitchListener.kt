package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.keyring.RPKKeyringService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val keyringService = Services[RPKKeyringService::class.java] ?: return
        val character = event.character
        if (character != null) {
            keyringService.loadKeyring(character).join()
        }
        val fromCharacter = event.fromCharacter
        if (fromCharacter != null) {
            keyringService.unloadKeyring(fromCharacter)
        }
    }

}