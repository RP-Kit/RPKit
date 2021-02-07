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

package com.rpkit.essentials.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent


class PlayerQuitListener(private val plugin: RPKEssentialsBukkit) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val logMessageService = Services[RPKLogMessageService::class.java] ?: return
        val quitMessage = event.quitMessage
        if (quitMessage != null) {
            plugin.server.onlinePlayers
                    .mapNotNull { player -> minecraftProfileService.getMinecraftProfile(player) }
                    .filter { minecraftProfile -> logMessageService.isLogMessagesEnabled(minecraftProfile) }
                    .forEach { minecraftProfile ->
                        minecraftProfile.sendMessage(quitMessage)
                    }
        }
        event.quitMessage = null
    }
}