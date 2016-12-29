/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.characters.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot.HAND

/**
 * Player interact entity listener for character cards.
 * This shows character cards upon right-clicking players.
 */
class PlayerInteractEntityListener(private val plugin: ElysiumCharactersBukkit): Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand == HAND) {
            if (event.player.isSneaking || !plugin.config.getBoolean("characters.view-card-requires-sneak")) {
                if (event.rightClicked is Player) {
                    if (event.player.hasPermission("elysium.characters.command.character.card.other")) {
                        val bukkitPlayer = event.rightClicked as Player
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                        val player = playerProvider.getPlayer(bukkitPlayer)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            val rightClicker = playerProvider.getPlayer(event.player)
                            character.showCharacterCard(rightClicker)
                        } else {
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character-other")))
                        }
                    } else {
                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-card-other")))
                    }
                }
            }
        }
    }
}
