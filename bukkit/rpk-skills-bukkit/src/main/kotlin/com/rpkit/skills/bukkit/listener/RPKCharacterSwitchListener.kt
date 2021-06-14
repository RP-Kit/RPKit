package com.rpkit.skills.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.skills.RPKSkillService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val skillService = Services[RPKSkillService::class.java] ?: return
        val newCharacter = event.character
        if (newCharacter != null) {
            skillService.loadSkillBindings(newCharacter).join()
            skillService.loadSkillCooldowns(newCharacter).join()
        }
        val oldCharacter = event.fromCharacter
        if (oldCharacter != null) {
            skillService.unloadSkillBindings(oldCharacter)
            skillService.unloadSkillCooldowns(oldCharacter)
        }
    }

}