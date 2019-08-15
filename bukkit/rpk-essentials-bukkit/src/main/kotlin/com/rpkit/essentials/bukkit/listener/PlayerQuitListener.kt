/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.essentials.bukkit.listener

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent




class PlayerQuitListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
        val quitMessage = event.quitMessage
        if (quitMessage != null) {
            plugin.server.onlinePlayers
                    .mapNotNull { player -> minecraftProfileProvider.getMinecraftProfile(player) }
                    .filter { minecraftProfile -> logMessageProvider.isLogMessagesEnabled(minecraftProfile) }
                    .forEach { minecraftProfile ->
                        minecraftProfile.sendMessage(quitMessage)
                    }
        }
        event.quitMessage = null
    }
}