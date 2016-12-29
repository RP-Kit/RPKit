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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * Player death listener for characters.
 * If server is configured to kill characters upon death, this will be done with this listener.
 */
class PlayerDeathListener(private val plugin: ElysiumCharactersBukkit): Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val player = playerProvider.getPlayer(event.entity)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null) {
            if (plugin.config.getBoolean("characters.kill-character-on-death")) {
                    character.isDead = true
                    characterProvider.updateCharacter(character)
            }
        }
    }

}
