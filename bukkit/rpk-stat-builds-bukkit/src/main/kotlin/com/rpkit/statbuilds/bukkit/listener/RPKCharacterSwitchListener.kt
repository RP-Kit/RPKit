package com.rpkit.statbuilds.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val statBuildService = Services[RPKStatBuildService::class.java] ?: return
        val newCharacter = event.character
        if (newCharacter != null) {
            statBuildService.loadStatPoints(newCharacter).join()
        }
        val oldCharacter = event.fromCharacter
        if (oldCharacter != null) {
            statBuildService.unloadStatPoints(oldCharacter)
        }
    }

}