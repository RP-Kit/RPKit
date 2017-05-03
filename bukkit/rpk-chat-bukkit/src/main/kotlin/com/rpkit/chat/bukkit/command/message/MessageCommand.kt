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

package com.rpkit.chat.bukkit.command.message

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupImpl
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Message command.
 * Messages a chat group or an individual.
 */
class MessageCommand(private val plugin: RPKChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size >= 2) {
            if (sender is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class)
                val senderMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (senderMinecraftProfile != null) {
                    var chatGroup = chatGroupProvider.getChatGroup(args[0])
                    if (chatGroup != null) {
                        if (sender.hasPermission("rpkit.chat.command.chatgroup.message")) {
                            if (chatGroup.memberMinecraftProfiles.contains(senderMinecraftProfile)) {
                                val message = StringBuilder()
                                for (i in 1..args.size - 1) {
                                    message.append(args[i]).append(" ")
                                }
                                chatGroup.sendMessage(senderMinecraftProfile, message.toString())
                            } else {
                                sender.sendMessage(plugin.messages["chat-group-message-invalid-not-a-member"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-permission-chat-group-message"])
                        }
                    } else if (plugin.server.getPlayer(args[0]) != null) {
                        if (sender.hasPermission("rpkit.chat.command.message")) {
                            val receiver = plugin.server.getPlayer(args[0])
                            val receiverMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(receiver)
                            if (receiverMinecraftProfile != null) {
                                if (receiverMinecraftProfile != senderMinecraftProfile) {
                                    val chatGroup1 = chatGroupProvider.getChatGroup("_pm_" + sender.name + "_" + receiver.name)
                                    val chatGroup2 = chatGroupProvider.getChatGroup("_pm_" + receiver.name + "_" + sender.name)
                                    if (chatGroup1 != null) {
                                        chatGroup = chatGroup1
                                    } else if (chatGroup2 != null) {
                                        chatGroup = chatGroup2
                                    } else {
                                        chatGroup = RPKChatGroupImpl(plugin, name = "_pm_" + sender.getName() + "_" + receiver.name)
                                        chatGroupProvider.addChatGroup(chatGroup)
                                        chatGroup.addMember(senderMinecraftProfile)
                                        chatGroup.addMember(receiverMinecraftProfile)
                                    }
                                    val message = StringBuilder()
                                    for (i in 1..args.size - 1) {
                                        message.append(args[i]).append(" ")
                                    }
                                    chatGroup.sendMessage(senderMinecraftProfile, message.toString())
                                } else {
                                    sender.sendMessage(plugin.messages["message-invalid-self"])
                                }
                            } else {
                                sender.sendMessage(plugin.messages["no-minecraft-profile"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-permission-message"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["message-invalid-target"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["message-usage"])
        }
        return true
    }
}