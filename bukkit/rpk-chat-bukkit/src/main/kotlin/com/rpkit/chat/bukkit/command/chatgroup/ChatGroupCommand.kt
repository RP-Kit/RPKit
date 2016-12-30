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
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Chat group command.
 * Parent command for all chat group management commands.
 */
class ChatGroupCommand(private val plugin: RPKChatBukkit): CommandExecutor {

    private val chatGroupCreateCommand = ChatGroupCreateCommand(plugin)
    private val chatGroupDisbandCommand = ChatGroupDisbandCommand(plugin)
    private val chatGroupInviteCommand = ChatGroupInviteCommand(plugin)
    private val chatGroupJoinCommand = ChatGroupJoinCommand(plugin)
    private val chatGroupLeaveCommand = ChatGroupLeaveCommand(plugin)
    private val chatGroupMessageCommand = ChatGroupMessageCommand(plugin)
    private val chatGroupMembersCommand = ChatGroupMembersCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.chat.command.chatgroup")) {
            if (args.size >= 1) {
                val newArgs = args.drop(1).toTypedArray()
                if (args[0].equals("create", ignoreCase = true)) {
                    chatGroupCreateCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("disband", ignoreCase = true)) {
                    chatGroupDisbandCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("invite", ignoreCase = true)) {
                    chatGroupInviteCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("join", ignoreCase = true)) {
                    chatGroupJoinCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("leave", ignoreCase = true)) {
                    chatGroupLeaveCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("message", ignoreCase = true)) {
                    chatGroupMessageCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("members", ignoreCase = true)) {
                    chatGroupMembersCommand.onCommand(sender, command, label, newArgs)
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chat-group-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chat-group-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chat-group")))
        }
        return true
    }

}