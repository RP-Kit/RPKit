package com.rpkit.shops.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.shops.bukkit.shopcount.RPKShopCountService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val shopCountService = Services[RPKShopCountService::class.java] ?: return
        val oldCharacter = event.fromCharacter
        if (oldCharacter != null) {
            shopCountService.unloadShopCount(oldCharacter)
        }
        val newCharacter = event.character
        if (newCharacter != null) {
            shopCountService.loadShopCount(newCharacter).join()
        }
    }

}