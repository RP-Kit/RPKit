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


class RPKVanishServiceImpl(override val plugin: RPKModerationBukkit) : RPKVanishService {

    override fun isVanished(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.database.getTable(RPKVanishStateTable::class.java)[minecraftProfile] != null
    }

    override fun setVanished(minecraftProfile: RPKMinecraftProfile, vanished: Boolean) {
        if (vanished) {
            val event = RPKBukkitVanishEvent(minecraftProfile)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
        } else {
            val event = RPKBukkitUnvanishEvent(minecraftProfile)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
        }
        val vanishStateTable = plugin.database.getTable(RPKVanishStateTable::class.java)
        var vanishState = vanishStateTable[minecraftProfile]
        if (vanishState == null) {
            vanishState = RPKVanishState(minecraftProfile)
            vanishStateTable.insert(vanishState)
        } else {
            vanishStateTable.delete(vanishState)
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val target = plugin.server.getPlayer(minecraftProfile.minecraftUUID) ?: return
        if (vanished) {
            for (observer in plugin.server.onlinePlayers) {
                minecraftProfileService.getMinecraftProfile(observer).thenAccept { observerMinecraftProfile ->
                    if (observerMinecraftProfile != null) {
                        if (!canSee(observerMinecraftProfile, minecraftProfile)) {
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                observer.hidePlayer(plugin, target)
                            })
                        }
                    }
                }
            }
        } else {
            for (observer in plugin.server.onlinePlayers) {
                observer.showPlayer(plugin, target)
            }
        }
    }

    override fun canSee(observer: RPKMinecraftProfile, target: RPKMinecraftProfile): Boolean {
        if (!isVanished(target)) return true
        val observerPlayer = plugin.server.getPlayer(observer.minecraftUUID) ?: return false
        val targetPlayer = plugin.server.getPlayer(target.minecraftUUID) ?: return false
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
        return observerTier <= targetTier
    }

}