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

package com.rpkit.chat.bukkit.command.unmute

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Unmute command.
 * Unmutes a chat channel.
 */
class UnmuteCommand(private val plugin: RPKChatBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (args.isNotEmpty()) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
                val player = playerProvider.getPlayer(sender)
                val chatChannel = chatChannelProvider.getChatChannel(args[0])
                if (chatChannel != null) {
                    if (sender.hasPermission("rpkit.chat.command.unmute.${chatChannel.name}")) {
                        chatChannel.addListener(player)
                        sender.sendMessage(plugin.messages["unmute-valid", mapOf(
                                Pair("channel", chatChannel.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.messages["no-permission-unmute", mapOf(
                                Pair("channel", chatChannel.name)
                        )])
                    }
                } else {
                    sender.sendMessage(plugin.messages["unmute-invalid-chatchannel"])
                }
            } else {
                sender.sendMessage(plugin.messages["unmute-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

}