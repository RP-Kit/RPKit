/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.bukkit.extension.closestChatColor
import com.rpkit.core.bukkit.extension.toColor
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.awt.Color
import java.util.regex.Pattern

/**
 * List chat channels command.
 * Lists available chat channels.
 */
class ListChatChannelsCommand(private val plugin: RPKChatBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.chat.command.listchatchannels")) {
            sender.sendMessage(plugin.messages["no-permission-listchatchannels"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        sender.sendMessage(plugin.messages["listchatchannels-title"])
        val chatChannelService = Services[RPKChatChannelService::class.java]
        if (chatChannelService == null) {
            sender.sendMessage(plugin.messages["no-chat-channel-service"])
            return true
        }
        chatChannelService.chatChannels.forEach { chatChannel ->
            chatChannel.listeners.thenAccept { listeners ->
                val messageComponents = mutableListOf<BaseComponent>()
                val pattern = Pattern.compile("(\\\$\\{channel\\})|(\\\$\\{mute\\})|(${ChatColor.COLOR_CHAR}x(${ChatColor.COLOR_CHAR}[0-9a-f]){6})|(${ChatColor.COLOR_CHAR}[0-9a-f])")
                val template = plugin.messages["listchatchannels-item", mapOf(
                    "color" to chatChannel.color.closestChatColor().toString()
                )]
                val matcher = pattern.matcher(template)
                var chatColor: ChatColor? = null
                var chatFormat: ChatColor? = null
                var index = 0
                while (matcher.find()) {
                    if (index != matcher.start()) {
                        val textComponent = TextComponent(template.substring(index, matcher.start()))
                        if (chatColor != null) {
                            textComponent.color = chatColor
                        }
                        if (chatFormat != null) {
                            textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                            textComponent.isBold = chatFormat == ChatColor.BOLD
                            textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                            textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                            textComponent.isItalic = chatFormat == ChatColor.ITALIC
                        }
                        messageComponents.add(textComponent)
                    }
                    if (matcher.group() == "\${channel}") {
                        val chatChannelComponent = TextComponent(chatChannel.name.value)
                        chatChannelComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatchannel ${chatChannel.name.value}")
                        chatChannelComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder().appendLegacy("Click to talk in ${chatChannel.name.value}").create())
                        if (chatColor != null) {
                            chatChannelComponent.color = chatColor
                        }
                        if (chatFormat != null) {
                            chatChannelComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                            chatChannelComponent.isBold = chatFormat == ChatColor.BOLD
                            chatChannelComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                            chatChannelComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                            chatChannelComponent.isItalic = chatFormat == ChatColor.ITALIC
                        }
                        messageComponents.add(chatChannelComponent)
                    } else if (matcher.group() == "\${mute}") {
                        if (listeners.any { listenerMinecraftProfile ->
                                listenerMinecraftProfile.id == minecraftProfile.id
                            }) {
                            val muteComponent = TextComponent("Mute")
                            muteComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mute ${chatChannel.name.value}")
                            muteComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder().appendLegacy("Click to mute ${chatChannel.name.value}").create())
                            if (chatColor != null) {
                                muteComponent.color = chatColor
                            }
                            if (chatFormat != null) {
                                muteComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                muteComponent.isBold = chatFormat == ChatColor.BOLD
                                muteComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                muteComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                muteComponent.isItalic = chatFormat == ChatColor.ITALIC
                            }
                            messageComponents.add(muteComponent)
                        } else {
                            val unmuteComponent = TextComponent("Unmute")
                            unmuteComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unmute ${chatChannel.name.value}")
                            unmuteComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder().appendLegacy("Click to unmute ${chatChannel.name.value}").create())
                            if (chatColor != null) {
                                unmuteComponent.color = chatColor
                            }
                            if (chatFormat != null) {
                                unmuteComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                unmuteComponent.isBold = chatFormat == ChatColor.BOLD
                                unmuteComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                unmuteComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                unmuteComponent.isItalic = chatFormat == ChatColor.ITALIC
                            }
                            messageComponents.add(unmuteComponent)
                        }
                    } else {
                        val match = matcher.group()
                        if (match.startsWith("${ChatColor.COLOR_CHAR}x") && match.length == 14) {
                            chatColor = Color.decode("#${match[3]}${match[5]}${match[7]}${match[9]}${match[11]}${match[13]}").closestChatColor()
                        } else {
                            val colorOrFormat = ChatColor.getByChar(match.drop(1)[0])
                            if (colorOrFormat?.toColor() != null) {
                                chatColor = colorOrFormat
                                chatFormat = null
                            }
                            if (colorOrFormat?.toColor() == null) {
                                chatFormat = colorOrFormat
                            }
                            if (colorOrFormat == ChatColor.RESET) {
                                chatColor = null
                                chatFormat = null
                            }
                        }
                    }
                    index = matcher.end()
                }
                val textComponent = TextComponent(template.substring(index, template.length))
                if (chatColor != null) {
                    textComponent.color = chatColor
                }
                if (chatFormat != null) {
                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                    textComponent.isBold = chatFormat == ChatColor.BOLD
                    textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                }
                messageComponents.add(textComponent)
                sender.spigot().sendMessage(*messageComponents.toTypedArray())
            }
        }
        return true
    }
}