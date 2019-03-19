/*
 * Copyright 2018 Ross Binden
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

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.table.RPKVanishStateTable
import com.rpkit.moderation.bukkit.event.vanish.RPKBukkitUnvanishEvent
import com.rpkit.moderation.bukkit.event.vanish.RPKBukkitVanishEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKMinecraftProfileEvent
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider


class RPKVanishProviderImpl(private val plugin: RPKModerationBukkit): RPKVanishProvider {

    override fun isVanished(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.core.database.getTable(RPKVanishStateTable::class).get(minecraftProfile)?.isVanished?:false
    }

    override fun setVanished(minecraftProfile: RPKMinecraftProfile, vanished: Boolean) {
        val event: RPKMinecraftProfileEvent = if (vanished) {
            val event = RPKBukkitVanishEvent(minecraftProfile)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            event
        } else {
            val event = RPKBukkitUnvanishEvent(minecraftProfile)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            event
        }
        val vanishStateTable = plugin.core.database.getTable(RPKVanishStateTable::class)
        var vanishState = vanishStateTable.get(event.minecraftProfile!!)
        if (vanishState == null) {
            vanishState = RPKVanishState(0, event.minecraftProfile!!, vanished)
            vanishStateTable.insert(vanishState)
        } else {
            vanishState.isVanished = vanished
            vanishStateTable.update(vanishState)
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val target = plugin.server.getPlayer(event.minecraftProfile!!.minecraftUUID)
        if (vanished) {
            for (observer in plugin.server.onlinePlayers) {
                val observerMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(observer)
                if (observerMinecraftProfile != null) {
                    if (!canSee(observerMinecraftProfile, event.minecraftProfile!!)) {
                        observer.hidePlayer(plugin, target)
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
        val observerPlayer = plugin.server.getPlayer(observer.minecraftUUID)
        val targetPlayer = plugin.server.getPlayer(target.minecraftUUID)
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