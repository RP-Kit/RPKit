package com.rpkit.unconsciousness.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val unconsciousnessService = Services[RPKUnconsciousnessService::class.java] ?: return
        val newCharacter = event.character
        if (newCharacter != null) {
            unconsciousnessService.loadUnconsciousness(newCharacter).join()
        }
        val oldCharacter = event.fromCharacter
        if (oldCharacter != null) {
            unconsciousnessService.unloadUnconsciousness(oldCharacter)
        }
    }

}