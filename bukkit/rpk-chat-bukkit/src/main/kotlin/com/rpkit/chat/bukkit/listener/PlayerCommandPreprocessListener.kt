/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.chat.bukkit.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.snooper.RPKSnooperService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/**
 * Player command preprocess listener.
 * Picks up commands before they are sent to normal chat processing, allowing them to be interpreted as chat channel
 * commands.
 * Hacky and circumvents the command system, but users are stuck in their ways.
 */
class PlayerCommandPreprocessListener(private val plugin: RPKChatBukkit) : Listener {

    @EventHandler
    fun onPlayerCommandPreProcess(event: PlayerCommandPreprocessEvent) {
        handleSnooping(event)
    }

    private fun handleSnooping(event: PlayerCommandPreprocessEvent) {
        val snooperService = Services[RPKSnooperService::class.java] ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val senderMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player) ?: return
        snooperService.snoopers.thenAccept { snoopers ->
            snoopers.filter(RPKMinecraftProfile::isOnline)
            .forEach { minecraftProfile ->
                minecraftProfile.sendMessage(plugin.messages.commandSnoop.withParameters(
                    sender = senderMinecraftProfile,
                    command = event.message
                ))
            }
        }
    }

}