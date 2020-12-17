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

package com.rpkit.chat.bukkit.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
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
        handleQuickChannelSwitch(event)
        handleSnooping(event)
    }

    private fun handleSnooping(event: PlayerCommandPreprocessEvent) {
        val snooperService = Services[RPKSnooperService::class.java] ?: return
        snooperService.snoopers
                .filter(RPKMinecraftProfile::isOnline)
                .forEach { minecraftProfile ->
                    minecraftProfile.sendMessage(plugin.messages["command-snoop", mapOf(
                            Pair("sender-player", event.player.name),
                            Pair("command", event.message)
                    )])
                }
    }

    private fun handleQuickChannelSwitch(event: PlayerCommandPreprocessEvent) {
        val chatChannelName = event.message.split(Regex("\\s+"))[0].drop(1)
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return
        val chatChannel = chatChannelService.getChatChannel(chatChannelName) ?: return
        if (!event.player.hasPermission("rpkit.chat.command.chatchannel.${chatChannel.name}")) {
            event.isCancelled = true
            event.player.sendMessage(plugin.messages["no-permission-chatchannel", mapOf(
                    Pair("channel", chatChannel.name)
            )])
            return
        }
        event.isCancelled = true
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
        if (minecraftProfile == null) {
            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
            return
        }
        val profile = minecraftProfile.profile
        if (event.message.startsWith("/$chatChannelName ")) {
            chatChannel.sendMessage(profile, minecraftProfile, event.message.split(Regex("\\s+")).drop(1).joinToString(" "))
        } else if (event.message.startsWith("/$chatChannelName")) {
            chatChannel.addSpeaker(minecraftProfile)
            event.player.sendMessage(plugin.messages["chatchannel-valid", mapOf(
                    Pair("channel", chatChannel.name)
            )])
        }
    }

}