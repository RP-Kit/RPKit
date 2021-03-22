/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupName
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Chat group members command.
 * Views members of a chat group.
 */
class ChatGroupMembersCommand(private val plugin: RPKChatBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.chat.command.chatgroup.members")) {
            sender.sendMessage(plugin.messages["no-permission-chat-group-members"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["chat-group-members-usage"])
            return true
        }
        val chatGroupService = Services[RPKChatGroupService::class.java]
        if (chatGroupService == null) {
            sender.sendMessage(plugin.messages["no-chat-group-service"])
            return true
        }
        chatGroupService.getChatGroup(RPKChatGroupName(args[0])).thenAccept { chatGroup ->
            if (chatGroup == null) {
                sender.sendMessage(plugin.messages["chat-group-members-invalid-chat-group"])
                return@thenAccept
            }
            sender.sendMessage(plugin.messages["chat-group-members-list-title"])
            chatGroup.members.thenAccept { members ->
                for (player in members) {
                    sender.sendMessage(plugin.messages["chat-group-members-list-item", mapOf(
                        "player" to player.name
                    )])
                }
            }
            sender.sendMessage(plugin.messages["chat-group-invitations-list-title"])
            chatGroup.invited.thenAccept { invited ->
                for (player in invited) {
                    sender.sendMessage(plugin.messages["chat-group-invitations-list-item", mapOf(
                        "player" to player.name
                    )])
                }
            }
        }
        return true
    }

}