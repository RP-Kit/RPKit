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

package com.rpkit.chat.bukkit.command.chatgroup

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Chat group message command.
 * Sends a message to a chat group.
 */
class ChatGroupMessageCommand(private val plugin: RPKChatBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.chat.command.chatgroup.message")) {
            if (args.size >= 2) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class)
                val chatGroup = chatGroupProvider.getChatGroup(args[0])
                if (chatGroup != null) {
                    if (sender is Player) {
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                        if (minecraftProfile != null) {
                            if (chatGroup.memberMinecraftProfiles.contains(minecraftProfile)) {
                                val message = StringBuilder()
                                for (i in 1 until args.size) {
                                    message.append(args[i]).append(" ")
                                }
                                chatGroup.sendMessage(minecraftProfile, message.toString())
                            } else {
                                sender.sendMessage(plugin.messages["chat-group-message-invalid-not-a-member"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-minecraft-profile"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["not-from-console"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["chat-group-message-invalid-chat-group"])
                }
            } else {
                sender.sendMessage(plugin.messages["chat-group-message-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-chat-group-message"])
        }
        return true
    }

}