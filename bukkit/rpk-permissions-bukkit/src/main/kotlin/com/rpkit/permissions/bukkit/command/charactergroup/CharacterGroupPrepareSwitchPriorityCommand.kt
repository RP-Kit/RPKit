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

package com.rpkit.permissions.bukkit.command.charactergroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.closestChatColor
import com.rpkit.core.bukkit.extension.levenshtein
import com.rpkit.core.bukkit.extension.toColor
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.groups
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.COLOR_CHAR
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.awt.Color

class CharacterGroupPrepareSwitchPriorityCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return true
        }
        if (args.size < 3) {
            sender.sendMessage(plugin.messages.characterGroupPrepareSwitchPriorityUsage)
            return true
        }
        val profileName = args.first()
        val characterName = args.drop(1).dropLast(1).joinToString(" ")
        val nameParts = profileName.split("#")
        val name = nameParts[0]
        val discriminator = nameParts[1].toIntOrNull()
        if (discriminator == null) {
            sender.sendMessage(plugin.messages.characterGroupViewInvalidProfileName)
            return true
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return true
        }
        profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).thenAccept getProfile@{ profile ->
            if (profile == null) {
                sender.sendMessage(plugin.messages.characterGroupViewInvalidProfile)
                return@getProfile
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                sender.sendMessage(plugin.messages.noCharacterService)
                return@getProfile
            }
            characterService.getCharacters(profile).thenAccept getCharacters@{ characters ->
                if (characters.isEmpty()) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getCharacters
                }
                val character = characters.minByOrNull { args.drop(1).joinToString(" ").levenshtein(it.name) }
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getCharacters
                }
                val group1 = args.last()
                character.groups.thenAccept getCharacterGroups@{ characterGroups ->
                    characterGroups.forEach { group ->
                        val message = plugin.messages.groupViewItem.withParameters(group = group)
                        val messageComponents = mutableListOf<BaseComponent>()
                        var chatColor: ChatColor? = null
                        var chatFormat: ChatColor? = null
                        var messageBuffer = StringBuilder()
                        var i = 0
                        while (i < message.length) {
                            if (message[i] == COLOR_CHAR) {
                                appendComponent(messageComponents, messageBuffer, chatColor, chatFormat)
                                messageBuffer = StringBuilder()
                                if (message[i + 1] == 'x') {
                                    chatColor =
                                        Color.decode("#${message[i + 2]}${message[i + 4]}${message[i + 6]}${message[i + 8]}${message[i + 10]}${message[i + 12]}").closestChatColor()
                                    i += 13
                                } else {
                                    val colorOrFormat = ChatColor.getByChar(message[i + 1])
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
                                    i += 2
                                }
                            } else if (message.substring(
                                    i,
                                    (i + "\${reorder}".length).coerceAtMost(message.length)
                                ) == "\${reorder}"
                            ) {
                                val reorderButton = TextComponent("\u292d").also {
                                    if (chatColor != null) {
                                        it.color = chatColor
                                    }
                                    if (chatFormat != null) {
                                        it.isObfuscated = chatFormat == ChatColor.MAGIC
                                        it.isBold = chatFormat == ChatColor.BOLD
                                        it.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                        it.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                        it.isItalic = chatFormat == ChatColor.ITALIC
                                    }
                                }
                                reorderButton.hoverEvent =
                                    HoverEvent(
                                        SHOW_TEXT,
                                        ComponentBuilder("Click to switch $group1 with ${group.name.value}").create()
                                    )
                                reorderButton.clickEvent = ClickEvent(
                                    RUN_COMMAND,
                                    "/charactergroup switchpriority $profileName $characterName $group1 ${group.name.value}"
                                )
                                messageComponents.add(reorderButton)
                                i += "\${reorder}".length
                            } else {
                                messageBuffer.append(message[i++])
                            }
                        }
                        appendComponent(messageComponents, messageBuffer, chatColor, chatFormat)
                        sender.spigot().sendMessage(*messageComponents.toTypedArray())
                    }
                }
            }
        }
        return true
    }

    private fun appendComponent(
        messageComponents: MutableList<BaseComponent>,
        messageBuffer: StringBuilder,
        chatColor: ChatColor?,
        chatFormat: ChatColor?
    ) {
        messageComponents.add(TextComponent(messageBuffer.toString()).also {
            if (chatColor != null) {
                it.color = chatColor
            }
            if (chatFormat != null) {
                it.isObfuscated = chatFormat == ChatColor.MAGIC
                it.isBold = chatFormat == ChatColor.BOLD
                it.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                it.isUnderlined = chatFormat == ChatColor.UNDERLINE
                it.isItalic = chatFormat == ChatColor.ITALIC
            }
        })
    }
}