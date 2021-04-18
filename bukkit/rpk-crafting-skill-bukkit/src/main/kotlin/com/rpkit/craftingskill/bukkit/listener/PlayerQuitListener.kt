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
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        characterService.getActiveCharacter(minecraftProfile).thenAccept { character ->
            if (character == null) return@thenAccept
            craftingSkillService.unloadCraftingExperience(character)
        }
    }

}