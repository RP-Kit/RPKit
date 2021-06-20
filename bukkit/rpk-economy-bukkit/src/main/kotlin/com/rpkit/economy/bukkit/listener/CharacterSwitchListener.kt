package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val economyService = Services[RPKEconomyService::class.java] ?: return
        val fromCharacter = event.fromCharacter
        val toCharacter = event.character
        if (toCharacter != null) {
            economyService.loadBalances(toCharacter)
        }
        if (fromCharacter != null) {
            economyService.unloadBalances(fromCharacter)
        }
    }

}