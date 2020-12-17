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

package com.rpkit.chat.bukkit.command.unmute

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Unmute command.
 * Unmutes a chat channel.
 */
class UnmuteCommand(private val plugin: RPKChatBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["unmute-usage"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val chatChannelService = Services[RPKChatChannelService::class.java]
        if (chatChannelService == null) {
            sender.sendMessage(plugin.messages["no-chat-channel-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val chatChannel = chatChannelService.getChatChannel(args[0])
        if (chatChannel == null) {
            sender.sendMessage(plugin.messages["unmute-invalid-chatchannel"])
            return true
        }
        if (!sender.hasPermission("rpkit.chat.command.unmute.${chatChannel.name}")) {
            sender.sendMessage(plugin.messages["no-permission-unmute", mapOf(
                    Pair("channel", chatChannel.name)
            )])
            return true
        }
        chatChannel.addListener(minecraftProfile)
        sender.sendMessage(plugin.messages["unmute-valid", mapOf(
                Pair("channel", chatChannel.name)
        )])
        return true
    }

}