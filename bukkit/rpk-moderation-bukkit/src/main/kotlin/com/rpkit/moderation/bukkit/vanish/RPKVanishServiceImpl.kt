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

package com.rpkit.moderation.bukkit.vanish

import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.table.RPKVanishStateTable
import com.rpkit.moderation.bukkit.event.vanish.RPKBukkitUnvanishEvent
import com.rpkit.moderation.bukkit.event.vanish.RPKBukkitVanishEvent
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture


class RPKVanishServiceImpl(override val plugin: RPKModerationBukkit) : RPKVanishService {

    override fun isVanished(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Boolean> {
        return plugin.database.getTable(RPKVanishStateTable::class.java)[minecraftProfile].thenApply { it != null }
    }

    override fun setVanished(minecraftProfile: RPKMinecraftProfile, vanished: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (vanished) {
                val event = RPKBukkitVanishEvent(minecraftProfile, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@runAsync
            } else {
                val event = RPKBukkitUnvanishEvent(minecraftProfile, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@runAsync
            }
            val vanishStateTable = plugin.database.getTable(RPKVanishStateTable::class.java)
            vanishStateTable[minecraftProfile].thenAccept { vanishState ->
                if (vanishState == null) {
                    vanishStateTable.insert(RPKVanishState(minecraftProfile))
                } else {
                    vanishStateTable.delete(vanishState)
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return@thenAccept
                val target = plugin.server.getPlayer(minecraftProfile.minecraftUUID) ?: return@thenAccept
                if (vanished) {
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        for (observer in plugin.server.onlinePlayers) {
                            minecraftProfileService.getMinecraftProfile(observer).thenAccept { observerMinecraftProfile ->
                                if (observerMinecraftProfile != null) {
                                    canSee(observerMinecraftProfile, minecraftProfile).thenAccept { canSee ->
                                        if (!canSee) {
                                            plugin.server.scheduler.runTask(plugin, Runnable {
                                                observer.hidePlayer(plugin, target)
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    })
                } else {
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        for (observer in plugin.server.onlinePlayers) {
                            observer.showPlayer(plugin, target)
                        }
                    })
                }
            }
        }
    }

    override fun canSee(observer: RPKMinecraftProfile, target: RPKMinecraftProfile): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            if (!isVanished(target).join()) return@supplyAsync true
            val observerPlayer = plugin.server.getPlayer(observer.minecraftUUID) ?: return@supplyAsync false
            val targetPlayer = plugin.server.getPlayer(target.minecraftUUID) ?: return@supplyAsync false
            var observerTier = 10
            var targetTier = 10
            for (tier in 9 downTo 1) {
                if (observerPlayer.hasPermission("rpkit.moderation.vanish.tier.$tier")) {
                    observerTier = tier
                }
                if (targetPlayer.hasPermission("rpkit.moderation.vanish.tier.$tier")) {
                    targetTier = tier
                }
            }
            return@supplyAsync observerTier <= targetTier
        }
    }

}