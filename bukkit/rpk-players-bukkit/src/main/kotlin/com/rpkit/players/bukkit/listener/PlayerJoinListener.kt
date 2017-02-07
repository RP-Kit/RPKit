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

package com.rpkit.players.bukkit.listener

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Player join listener for creating player instance.
 */
class PlayerJoinListener(private val plugin: RPKPlayersBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // If RPK doesn't have a player for Bukkit's player, create it here so that the player can log on through
        // web UI. If RPK DOES have a player, update the last known IP.
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val player = playerProvider.getPlayer(event.player)
        player.lastKnownIP = event.player.address?.address?.hostAddress
        playerProvider.updatePlayer(player)
    }

}
