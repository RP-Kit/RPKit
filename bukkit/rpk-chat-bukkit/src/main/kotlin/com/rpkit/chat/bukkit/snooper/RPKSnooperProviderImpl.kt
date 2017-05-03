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

package com.rpkit.chat.bukkit.snooper

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKSnooperTable
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.Bukkit

/**
 * Snooper provider implementation.
 */
class RPKSnooperProviderImpl(private val plugin: RPKChatBukkit): RPKSnooperProvider {

    override val snoopers: List<RPKPlayer>
        get() = snooperMinecraftProfiles.map { minecraftProfile ->
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            playerProvider.getPlayer(Bukkit.getOfflinePlayer(minecraftProfile.minecraftUUID))
        }

    override val snooperMinecraftProfiles: List<RPKMinecraftProfile>
        get() = plugin.core.database.getTable(RPKSnooperTable::class).getAll().map(RPKSnooper::minecraftProfile)

    override fun addSnooper(player: RPKPlayer) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                addSnooper(minecraftProfile)
            }
        }
    }

    override fun addSnooper(minecraftProfile: RPKMinecraftProfile) {
        if (!snooperMinecraftProfiles.contains(minecraftProfile)) {
            plugin.core.database.getTable(RPKSnooperTable::class).insert(RPKSnooper(minecraftProfile = minecraftProfile))
        }
    }

    override fun removeSnooper(player: RPKPlayer) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                removeSnooper(minecraftProfile)
            }
        }
    }

    override fun removeSnooper(minecraftProfile: RPKMinecraftProfile) {
        val snooperTable = plugin.core.database.getTable(RPKSnooperTable::class)
        val snooper = snooperTable.get(minecraftProfile)
        if (snooper != null) {
            snooperTable.delete(snooper)
        }
    }

    override fun isSnooping(player: RPKPlayer): Boolean {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return isSnooping(minecraftProfile)
            }
        }
        return false
    }

    override fun isSnooping(minecraftProfile: RPKMinecraftProfile): Boolean {
        val snooperTable = plugin.core.database.getTable(RPKSnooperTable::class)
        return snooperTable.get(minecraftProfile) != null
    }

}