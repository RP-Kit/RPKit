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

package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ChatChannelCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {

    private val chatChannelJoinCommand: ChatChannelJoinCommand
    private val chatChannelLeaveCommand: ChatChannelLeaveCommand
    private val chatChannelSpeakCommand: ChatChannelSpeakCommand
    private val chatChannelCreateCommand: ChatChannelCreateCommand
    private val chatChannelDeleteCommand: ChatChannelDeleteCommand
    private val chatChannelListCommand: ChatChannelListCommand
    private val chatChannelSetCommand: ChatChannelSetCommand

    init {
        chatChannelJoinCommand = ChatChannelJoinCommand(plugin)
        chatChannelLeaveCommand = ChatChannelLeaveCommand(plugin)
        chatChannelSpeakCommand = ChatChannelSpeakCommand(plugin)
        chatChannelCreateCommand = ChatChannelCreateCommand(plugin)
        chatChannelDeleteCommand = ChatChannelDeleteCommand(plugin)
        chatChannelListCommand = ChatChannelListCommand(plugin)
        chatChannelSetCommand = ChatChannelSetCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            var newArgsList: MutableList<String> = arrayListOf()
            for (i: Int in 1..(args.size - 1)) {
                newArgsList.add(args[i])
            }
            var newArgs = newArgsList.toTypedArray()
            if (args[0].equals("join", ignoreCase = true) || args[0].equals("unmute", ignoreCase = true)) {
                return chatChannelJoinCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("leave", ignoreCase = true) || args[0].equals("mute", ignoreCase = true)) {
                return chatChannelLeaveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("speak", ignoreCase = true) || args[0].equals("talk", ignoreCase = true)) {
                return chatChannelSpeakCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true) || args[0].equals("add", ignoreCase = true)) {
                return chatChannelCreateCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("delete", ignoreCase = true) || args[0].equals("remove", ignoreCase = true)) {
                return chatChannelDeleteCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return chatChannelListCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("set", ignoreCase = true) || args[0].equals("modify", ignoreCase = true)) {
                return chatChannelSetCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-usage")))
        }
        return true
    }

}
