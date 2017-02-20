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

package com.rpkit.chat.bukkit.command.listchatchannels

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.core.bukkit.util.closestChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * List chat channels command.
 * Lists available chat channels.
 */
class ListChatChannelsCommand(private val plugin: RPKChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.chat.command.listchatchannels")) {
            sender.sendMessage(plugin.messages["listchatchannels-title"])
            plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class).chatChannels.forEach { chatChannel ->
                sender.sendMessage(plugin.messages["listchatchannels-item", mapOf(
                        Pair("channel", chatChannel.name),
                        Pair("color", chatChannel.color.closestChatColor().toString())
                )])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-listchatchannels"])
        }
        return true
    }
}