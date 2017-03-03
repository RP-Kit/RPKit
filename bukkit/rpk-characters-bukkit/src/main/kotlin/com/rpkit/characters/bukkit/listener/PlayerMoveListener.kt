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

package com.rpkit.characters.bukkit.listener

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.BLINDNESS

/**
 * Player move listener for preventing players from moving about with a dead character.
 */
class PlayerMoveListener(private val plugin: RPKCharactersBukkit): Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val player = playerProvider.getPlayer(event.player)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null && character.isDead) {
            if (event.from.blockX != event.to.blockX || event.from.blockZ != event.to.blockZ) {
                event.player.teleport(Location(event.from.world, event.from.blockX + 0.5, event.from.blockY + 0.5, event.from.blockZ.toDouble(), event.from.yaw, event.from.pitch))
                event.player.sendMessage(plugin.messages["dead-character"])
                event.player.addPotionEffect(PotionEffect(BLINDNESS, 60, 1))
            }
        }
    }

}
