package com.rpkit.craftingskill.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: RPKCraftingSkillBukkit) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val craftingSkillService = Services[RPKCraftingSkillService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) return@getMinecraftProfile
            characterService.getActiveCharacter(minecraftProfile).thenAccept getActiveCharacter@{ character ->
                if (character == null) return@getActiveCharacter
                // If a player relogs quickly, then by the time the data has been retrieved, the player is sometimes back
                // online. We only want to unload data if the player is offline.
                if (!minecraftProfile.isOnline) {
                    craftingSkillService.unloadCraftingExperience(character)
                }
            }
        }
    }

}