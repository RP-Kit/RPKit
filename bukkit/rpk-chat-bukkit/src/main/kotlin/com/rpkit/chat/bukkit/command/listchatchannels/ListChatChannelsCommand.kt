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
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
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
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
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
            val messageComponents = mutableListOf<BaseComponent>()
            val pattern = Pattern.compile("(\\\$channel)|(\\\$mute)|(${ChatColor.COLOR_CHAR}[0-9a-f])")
            val template = plugin.messages["listchatchannels-item", mapOf(
                "color" to ChatColor.of(chatChannel.color).toString()
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
                if (matcher.group() == "\$channel") {
                    val chatChannelComponent = TextComponent(chatChannel.name)
                    chatChannelComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatchannel ${chatChannel.name}")
                    chatChannelComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to talk in ${chatChannel.name}"))
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
                } else if (matcher.group() == "\$mute") {
                    if (chatChannel.listenerMinecraftProfiles.any { listenerMinecraftProfile ->
                                listenerMinecraftProfile.id == minecraftProfile.id
                            }) {
                        val muteComponent = TextComponent("Mute")
                        muteComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mute ${chatChannel.name}")
                        muteComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to mute ${chatChannel.name}"))
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
                        unmuteComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unmute ${chatChannel.name}")
                        unmuteComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to unmute ${chatChannel.name}"))
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
                    val colorOrFormat = ChatColor.getByChar(matcher.group().drop(1)[0])
                    if (colorOrFormat?.color != null) {
                        chatColor = colorOrFormat
                        chatFormat = null
                    }
                    if (colorOrFormat?.color == null) {
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
        return true
    }
}