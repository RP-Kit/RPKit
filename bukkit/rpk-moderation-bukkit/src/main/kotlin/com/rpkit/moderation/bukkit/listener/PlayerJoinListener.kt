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

import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.vanish.RPKVanishService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerJoinListener(private val plugin: RPKModerationBukkit) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val vanishService = Services[RPKVanishService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept { observer ->
            if (observer == null) return@thenAccept
            plugin.server.scheduler.runTask(plugin, Runnable {
                plugin.server.onlinePlayers
                    .filter { player -> event.player != player }
                    .forEach { player ->
                        minecraftProfileService.getMinecraftProfile(player).thenAccept { target ->
                            if (target != null) {
                                if (!vanishService.canSee(observer, target)) {
                                    plugin.server.scheduler.runTask(plugin, Runnable {
                                        event.player.hidePlayer(plugin, player)
                                    })
                                }
                                if (!vanishService.canSee(target, observer)) {
                                    plugin.server.scheduler.runTask(plugin, Runnable {
                                        player.hidePlayer(plugin, event.player)
                                    })
                                }
                            }
                        }

                    }
                if (vanishService.isVanished(observer)) {
                    event.player.sendMessage(plugin.messages["vanish-invisible"])
                }
            })
        }
    }

}