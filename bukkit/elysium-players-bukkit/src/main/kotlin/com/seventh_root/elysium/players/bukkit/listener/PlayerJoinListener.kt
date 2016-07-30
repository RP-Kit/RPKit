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

package com.seventh_root.elysium.players.bukkit.listener

import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: ElysiumPlayersBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // If Elysium doesn't have a player for Bukkit's player, create it here so that the player can log on through
        // web UI. If Elysium DOES have a player, update the last known IP.
        plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class).getPlayer(event.player)
    }

}
