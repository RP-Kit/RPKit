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

import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: RPKEssentialsBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
        val joinMessage = event.joinMessage
        if (joinMessage != null) {
            plugin.server.onlinePlayers.mapNotNull { player -> minecraftProfileProvider.getMinecraftProfile(player) }
                    .filter { minecraftProfile -> logMessageProvider.isLogMessagesEnabled(minecraftProfile) }
                    .forEach { minecraftProfile ->
                        minecraftProfile.sendMessage(joinMessage)
                    }
        }
        event.joinMessage = null
        val dailyQuoteProvider = plugin.core.serviceManager.getServiceProvider(RPKDailyQuoteProvider::class)
        event.player.sendMessage(dailyQuoteProvider.getDailyQuote())
    }

}