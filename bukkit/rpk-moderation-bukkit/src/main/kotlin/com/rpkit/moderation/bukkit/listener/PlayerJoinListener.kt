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

package com.rpkit.moderation.bukkit.listener

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.vanish.RPKVanishProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerJoinListener(private val plugin: RPKModerationBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val vanishProvider = plugin.core.serviceManager.getServiceProvider(RPKVanishProvider::class)
        val observer = minecraftProfileProvider.getMinecraftProfile(event.player)
        if (observer != null) {
            plugin.server.onlinePlayers
                    .filter { player -> event.player != player }
                    .forEach { player ->
                val target = minecraftProfileProvider.getMinecraftProfile(player)
                if (target != null) {
                    if (!vanishProvider.canSee(observer, target)) {
                        event.player.hidePlayer(plugin, player)
                    }
                    if (!vanishProvider.canSee(target, observer)) {
                        player.hidePlayer(plugin, event.player)
                    }
                }
            }
            if (vanishProvider.isVanished(observer)) {
                event.player.sendMessage(plugin.messages["vanish-invisible"])
            }
        }
    }

}