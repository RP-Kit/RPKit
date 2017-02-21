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
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import mkremins.fanciful.FancyMessage
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern

/**
 * List chat channels command.
 * Lists available chat channels.
 */
class ListChatChannelsCommand(private val plugin: RPKChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.chat.command.listchatchannels")) {
            if (sender is Player) {

                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val player = playerProvider.getPlayer(sender)
                sender.sendMessage(plugin.messages["listchatchannels-title"])
                plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class).chatChannels.forEach { chatChannel ->
                    val message = FancyMessage("")
                    val pattern = Pattern.compile("(\\\$channel)|(\\\$mute)|(${ChatColor.COLOR_CHAR}[0-9a-f])")
                    val template = plugin.messages["listchatchannels-item", mapOf(
                            Pair("color", chatChannel.color.closestChatColor().toString())
                    )]
                    val matcher = pattern.matcher(template)
                    var chatColor: ChatColor? = null
                    var chatFormat: ChatColor? = null
                    var index = 0
                    while (matcher.find()) {
                        if (index != matcher.start()) {
                            message.then(template.substring(index, matcher.start()))
                            if (chatColor != null) {
                                message.color(chatColor)
                            }
                            if (chatFormat != null) {
                                message.style(chatFormat)
                            }
                        }
                        if (matcher.group() == "\$channel") {
                            message.then(chatChannel.name)
                                    .command("/chatchannel ${chatChannel.name}")
                                    .tooltip("Click to talk in ${chatChannel.name}")
                            if (chatColor != null) {
                                message.color(chatColor)
                            }
                            if (chatFormat != null) {
                                message.style(chatFormat)
                            }
                        } else if (matcher.group() == "\$mute") {
                            if (chatChannel.listeners.contains(player)) {
                                message.then("Mute")
                                        .command("/mute ${chatChannel.name}")
                                        .tooltip("Click to mute ${chatChannel.name}")
                                if (chatColor != null) {
                                    message.color(chatColor)
                                }
                                if (chatFormat != null) {
                                    message.style(chatFormat)
                                }
                            } else {
                                message.then("Unmute")
                                        .command("/unmute ${chatChannel.name}")
                                        .tooltip("Click to unmute ${chatChannel.name}")
                                if (chatColor != null) {
                                    message.color(chatColor)
                                }
                                if (chatFormat != null) {
                                    message.style(chatFormat)
                                }
                            }
                        } else {
                            val colorOrFormat = ChatColor.getByChar(matcher.group().drop(1))
                            if (colorOrFormat.isColor) {
                                chatColor = colorOrFormat
                                chatFormat = null
                            }
                            if (colorOrFormat.isFormat) {
                                chatFormat = colorOrFormat
                            }
                            if (colorOrFormat == ChatColor.RESET) {
                                chatColor = null
                                chatFormat = null
                            }
                        }
                        index = matcher.end()
                    }
                    message.then(template.substring(index, template.length))
                    if (chatColor != null) {
                        message.color(chatColor)
                    }
                    if (chatFormat != null) {
                        message.style(chatFormat)
                    }
                    message.send(sender)
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-listchatchannels"])
        }
        return true
    }
}