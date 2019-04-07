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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider


class RPKVanishProviderImpl(private val plugin: RPKModerationBukkit): RPKVanishProvider {

    override fun isVanished(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.core.database.getTable(RPKVanishStateTable::class).get(minecraftProfile)?.isVanished?:false
    }

    override fun setVanished(minecraftProfile: RPKMinecraftProfile, vanished: Boolean) {
        val vanishStateTable = plugin.core.database.getTable(RPKVanishStateTable::class)
        var vanishState = vanishStateTable.get(minecraftProfile)
        if (vanishState == null) {
            vanishState = RPKVanishState(0, minecraftProfile, vanished)
            vanishStateTable.insert(vanishState)
        } else {
            vanishState.isVanished = vanished
            vanishStateTable.update(vanishState)
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val target = plugin.server.getPlayer(minecraftProfile.minecraftUUID) ?: return
        if (vanished) {
            for (observer in plugin.server.onlinePlayers) {
                val observerMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(observer)
                if (observerMinecraftProfile != null) {
                    if (!canSee(observerMinecraftProfile, minecraftProfile)) {
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