package com.rpkit.experience.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerJoinListener(private val plugin: RPKExperienceBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        val player = playerProvider.getPlayer(event.player)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null) {
            event.player.level = experienceProvider.getLevel(character)
            event.player.exp = (experienceProvider.getExperience(character) - experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character))).toFloat() / (experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character) + 1) - experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character))).toFloat()
        }
    }

}