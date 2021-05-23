package com.rpkit.experience.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener(private val plugin: RPKExperienceBukkit) : Listener {

    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        val newCharacter = event.character
        if (newCharacter != null) {
            experienceService.loadExperience(newCharacter).join()
            val characterLevel = experienceService.getPreloadedLevel(newCharacter) ?: 1
            val characterExperience = experienceService.getPreloadedExperience(newCharacter) ?: 0
            plugin.server.scheduler.runTask(plugin, Runnable {
                val minecraftProfile = event.minecraftProfile
                val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
                if (bukkitPlayer != null) {
                    bukkitPlayer.level = characterLevel
                    bukkitPlayer.exp =
                        (characterExperience - experienceService.getExperienceNeededForLevel(characterLevel)).toFloat() /
                                (experienceService.getExperienceNeededForLevel(characterLevel + 1) - experienceService.getExperienceNeededForLevel(
                                    characterLevel
                                )).toFloat()
                }
            })
        }
        val oldCharacter = event.fromCharacter
        if (oldCharacter != null) {
            experienceService.unloadExperience(oldCharacter)
        }
    }

}