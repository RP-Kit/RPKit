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
import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteService
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: RPKEssentialsBukkit) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        sendJoinMessage(event)
        sendDailyQuote(event)
    }

    private fun sendJoinMessage(event: PlayerJoinEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        val logMessageService = Services[RPKLogMessageService::class.java]
        if (logMessageService != null && minecraftProfileService != null) {
            val joinMessage = event.joinMessage
            if (joinMessage != null) {
                plugin.server.onlinePlayers.mapNotNull { player -> minecraftProfileService.getPreloadedMinecraftProfile(player) }
                    .forEach { minecraftProfile ->
                        logMessageService.isLogMessagesEnabled(minecraftProfile).thenAccept { isLogMessagesEnabled ->
                            if (isLogMessagesEnabled) {
                                minecraftProfile.sendMessage(joinMessage)
                            }
                        }
                    }
            }
            event.joinMessage = null
        }
    }

    private fun sendDailyQuote(event: PlayerJoinEvent) {
        val dailyQuoteService = Services[RPKDailyQuoteService::class.java]
        if (dailyQuoteService != null) {
            event.player.sendMessage(dailyQuoteService.getDailyQuote())
        }
    }

}