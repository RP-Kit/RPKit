package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.CompletableFuture

class RPKCharacterSwitchListener : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val oldCharacter = event.fromCharacter
        val newCharacter = event.character
        val professionService = Services[RPKProfessionService::class.java] ?: return
        if (oldCharacter != null) {
            val professions = professionService.getProfessions(oldCharacter).join()
            professions.forEach { profession ->
                professionService.unloadProfessionExperience(oldCharacter, profession)
            }
            professionService.unloadProfessions(oldCharacter)
        }
        if (newCharacter != null) {
            professionService.loadProfessions(newCharacter).thenAccept { professions ->
                CompletableFuture.allOf(*professions.map { profession ->
                    professionService.loadProfessionExperience(newCharacter, profession)
                }.toTypedArray()).join()
            }.join()
        }
    }

}