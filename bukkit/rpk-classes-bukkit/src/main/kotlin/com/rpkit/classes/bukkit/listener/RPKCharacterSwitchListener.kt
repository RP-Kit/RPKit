package com.rpkit.classes.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val classService = Services[RPKClassService::class.java] ?: return
        val newCharacter = event.character
        if (newCharacter != null) {
            val `class` = classService.loadClass(newCharacter).join()
            if (`class` != null) {
                classService.loadExperience(newCharacter, `class`).join()
            }
        }
        val oldCharacter = event.fromCharacter
        if (oldCharacter != null) {
            val `class` = classService.getClass(oldCharacter).join()
            if (`class` != null) {
                classService.unloadExperience(oldCharacter, `class`)
            }
            classService.unloadClass(oldCharacter)
        }
    }

}