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

package com.seventh_root.elysium.chat.bukkit.command.message

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroupImpl
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroupProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Message command.
 * Messages a chat group or an individual.
 */
class MessageCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size >= 2) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatGroupProvider::class)
                val senderPlayer = playerProvider.getPlayer(sender)
                var chatGroup = chatGroupProvider.getChatGroup(args[0])
                if (chatGroup != null) {
                    if (sender.hasPermission("elysium.chat.command.chatgroup.message")) {
                        if (chatGroup.members.contains(senderPlayer)) {
                            val message = StringBuilder()
                            for (i in 1..args.size - 1) {
                                message.append(args[i]).append(" ")
                            }
                            chatGroup.sendMessage(senderPlayer, message.toString())
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chat-group-message-invalid-not-a-member")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chat-group-message")))
                    }
                } else if (plugin.server.getPlayer(args[0]) != null) {
                    if (sender.hasPermission("elysium.chat.command.message")) {
                        val receiver = plugin.server.getPlayer(args[0])
                        val receiverPlayer = playerProvider.getPlayer(receiver)
                        if (receiverPlayer != senderPlayer) {
                            val chatGroup1 = chatGroupProvider.getChatGroup("_pm_" + sender.name + "_" + receiver.name)
                            val chatGroup2 = chatGroupProvider.getChatGroup("_pm_" + receiver.name + "_" + sender.name)
                            if (chatGroup1 != null) {
                                chatGroup = chatGroup1
                            } else if (chatGroup2 != null) {
                                chatGroup = chatGroup2
                            } else {
                                chatGroup = ElysiumChatGroupImpl(plugin, name = "_pm_" + sender.getName() + "_" + receiver.name)
                                chatGroupProvider.addChatGroup(chatGroup)
                                chatGroup.addMember(senderPlayer)
                                chatGroup.addMember(receiverPlayer)
                            }
                            val message = StringBuilder()
                            for (i in 1..args.size - 1) {
                                message.append(args[i]).append(" ")
                            }
                            chatGroup.sendMessage(senderPlayer, message.toString())
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.message-invalid-self")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-message")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.message-invalid-target")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.message-usage")))
        }
        return true
    }
}