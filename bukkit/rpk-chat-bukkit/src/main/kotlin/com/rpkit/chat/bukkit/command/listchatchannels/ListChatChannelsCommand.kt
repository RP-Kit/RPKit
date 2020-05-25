/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
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
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    sender.sendMessage(plugin.messages["listchatchannels-title"])
                    plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class).chatChannels.forEach { chatChannel ->
                        val messageComponents = mutableListOf<BaseComponent>()
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
                                val textComponent = TextComponent(template.substring(index, matcher.start()))
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
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
                            if (matcher.group() == "\$channel") {
                                val chatChannelComponent = TextComponent(chatChannel.name)
                                chatChannelComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatchannel ${chatChannel.name}")
                                chatChannelComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to talk in ${chatChannel.name}")))
                                if (chatColor != null) {
                                    chatChannelComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    chatChannelComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    chatChannelComponent.isBold = chatFormat == ChatColor.BOLD
                                    chatChannelComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                    chatChannelComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    chatChannelComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(chatChannelComponent)
                            } else if (matcher.group() == "\$mute") {
                                if (chatChannel.listenerMinecraftProfiles.any { listenerMinecraftProfile ->
                                            listenerMinecraftProfile.id == minecraftProfile.id }) {
                                    val muteComponent = TextComponent("Mute")
                                    muteComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mute ${chatChannel.name}")
                                    muteComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to mute ${chatChannel.name}")))
                                    if (chatColor != null) {
                                        muteComponent.color = chatColor.asBungee()
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
                                    unmuteComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unmute ${chatChannel.name}")
                                    unmuteComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to unmute ${chatChannel.name}")))
                                    if (chatColor != null) {
                                        unmuteComponent.color = chatColor.asBungee()
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
                                val colorOrFormat = ChatColor.getByChar(matcher.group().drop(1))
                                if (colorOrFormat?.isColor == true) {
                                    chatColor = colorOrFormat
                                    chatFormat = null
                                }
                                if (colorOrFormat?.isFormat == true) {
                                    chatFormat = colorOrFormat
                                }
                                if (colorOrFormat == ChatColor.RESET) {
                                    chatColor = null
                                    chatFormat = null
                                }
                            }
                            index = matcher.end()
                        }
                        val textComponent = TextComponent(template.substring(index, template.length))
                        if (chatColor != null) {
                            textComponent.color = chatColor.asBungee()
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
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
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