/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.characters.bukkit.listener

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot.HAND

/**
 * Player interact entity listener for character cards.
 * This shows character cards upon right-clicking players.
 */
class PlayerInteractEntityListener(private val plugin: RPKCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != HAND) return
        if (!event.player.isSneaking && plugin.config.getBoolean("characters.view-card-requires-sneak")) return
        if (event.rightClicked !is Player) return
        if (!event.player.hasPermission("rpkit.characters.command.character.card.other")) {
            event.player.sendMessage(plugin.messages["no-permission-character-card-other"])
            return
        }
        val bukkitPlayer = event.rightClicked as Player
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return
        }
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            event.player.sendMessage(plugin.messages["no-character-service"])
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
            return
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            event.player.sendMessage(plugin.messages["no-character-other"])
            return
        }
        val rightClicker = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        character.showCharacterCard(rightClicker)
    }
}
