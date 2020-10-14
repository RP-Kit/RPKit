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

package com.rpkit.chat.bukkit.command.chatchannel

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Chat channel command.
 * Sets which chat channel a player is speaking in.
 */
class ChatChannelCommand(private val plugin: RPKChatBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["chatchannel-usage"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val chatChannelService = Services[RPKChatChannelService::class]
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
            sender.sendMessage(plugin.messages["chatchannel-invalid-chatchannel"])
            return true
        }
        if (!sender.hasPermission("rpkit.chat.command.chatchannel.${chatChannel.name}")) {
            sender.sendMessage(plugin.messages["no-permission-chatchannel", mapOf(
                    Pair("channel", chatChannel.name)
            )])
            return true
        }
        chatChannel.addSpeaker(minecraftProfile)
        sender.sendMessage(plugin.messages["chatchannel-valid", mapOf(
                Pair("channel", chatChannel.name)
        )])
        return true
    }

}