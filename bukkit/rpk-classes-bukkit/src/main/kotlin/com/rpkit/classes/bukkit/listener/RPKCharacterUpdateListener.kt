package com.rpkit.classes.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterUpdateEvent
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterUpdateListener(private val plugin: RPKClassesBukkit) : Listener {

    @EventHandler
    fun onCharacterUpdate(event: RPKBukkitCharacterUpdateEvent) {
        val classService = Services[RPKClassService::class.java] ?: return
        val `class` = classService.getClass(event.character).join() ?: return
        if (event.character.age < `class`.minAge || event.character.age > `class`.maxAge) {
            event.character.minecraftProfile?.sendMessage(plugin.messages.classSetInvalidAge.withParameters(
                maxAge = `class`.maxAge, minAge = `class`.minAge
            ))
            event.isCancelled = true
        }
    }

}