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

class ChatChannelSetCommand(private val plugin: ElysiumChatBukkit): CommandExecutor {

    private val chatChannelSetNameCommand: ChatChannelSetNameCommand
    private val chatChannelSetColorCommand: ChatChannelSetColorCommand
    private val chatChannelSetFormatCommand: ChatChannelSetFormatCommand
    private val chatChannelSetRadiusCommand: ChatChannelSetRadiusCommand
    private val chatChannelSetClearRadiusCommand: ChatChannelSetClearRadiusCommand
    private val chatChannelSetMatchPatternCommand: ChatChannelSetMatchPatternCommand
    private val chatChannelSetIRCEnabledCommand: ChatChannelSetIRCEnabledCommand
    private val chatChannelSetIRCChannelCommand: ChatChannelSetIRCChannelCommand
    private val chatChannelSetIRCWhitelistCommand: ChatChannelSetIRCWhitelistCommand
    private val chatChannelSetJoinedByDefaultCommand: ChatChannelSetJoinedByDefaultCommand

    init {
        chatChannelSetNameCommand = ChatChannelSetNameCommand(plugin)
        chatChannelSetColorCommand = ChatChannelSetColorCommand(plugin)
        chatChannelSetFormatCommand = ChatChannelSetFormatCommand(plugin)
        chatChannelSetRadiusCommand = ChatChannelSetRadiusCommand(plugin)
        chatChannelSetClearRadiusCommand = ChatChannelSetClearRadiusCommand(plugin)
        chatChannelSetMatchPatternCommand = ChatChannelSetMatchPatternCommand(plugin)
        chatChannelSetIRCEnabledCommand = ChatChannelSetIRCEnabledCommand(plugin)
        chatChannelSetIRCChannelCommand = ChatChannelSetIRCChannelCommand(plugin)
        chatChannelSetIRCWhitelistCommand = ChatChannelSetIRCWhitelistCommand(plugin)
        chatChannelSetJoinedByDefaultCommand = ChatChannelSetJoinedByDefaultCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("elysium.chat.command.chatchannel.set")) {
            if (args.size > 0) {
                var newArgsList: MutableList<String> = arrayListOf()
                for (i: Int in 1..(args.size - 1)) {
                    newArgsList.add(args[i])
                }
                var newArgs = newArgsList.toTypedArray()
                if (args[0].equals("name", ignoreCase = true)) {
                    chatChannelSetNameCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("color", ignoreCase = true) || args[0].equals("colour", ignoreCase = true)) {
                    chatChannelSetColorCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("format", ignoreCase = true)) {
                    chatChannelSetFormatCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("radius", ignoreCase = true)) {
                    chatChannelSetRadiusCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("clearradius", ignoreCase = true)) {
                    chatChannelSetClearRadiusCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("matchpattern", ignoreCase = true)) {
                    chatChannelSetMatchPatternCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("ircenabled", ignoreCase = true)) {
                    chatChannelSetIRCEnabledCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("ircchannel", ignoreCase = true)) {
                    chatChannelSetIRCChannelCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("ircwhitelist", ignoreCase = true)) {
                    chatChannelSetIRCWhitelistCommand.onCommand(sender, command, label, newArgs)
                } else if (args[0].equals("joinedbydefault", ignoreCase = true)) {
                    chatChannelSetJoinedByDefaultCommand.onCommand(sender, command, label, newArgs)
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-set")))
        }
        return true
    }
}
