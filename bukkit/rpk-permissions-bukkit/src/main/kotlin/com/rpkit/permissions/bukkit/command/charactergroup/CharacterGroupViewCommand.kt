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
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.awt.Color

class CharacterGroupViewCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.character.group.view")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterGroupView)
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages.characterGroupViewUsage)
            return true
        }
        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return true
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return true
        }
        if (!args[0].contains("#")) {
            sender.sendMessage(plugin.messages.characterGroupViewInvalidProfileName)
            return true
        }
        val nameParts = args[0].split("#")
        val name = nameParts[0]
        val discriminator = nameParts[1].toIntOrNull()
        if (discriminator == null) {
            sender.sendMessage(plugin.messages.characterGroupViewInvalidProfileName)
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
            characterService.getCharacters(profile).thenAcceptAsync getCharacters@{ characters ->
                if (characters.isEmpty()) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getCharacters
                }
                val character = characters.minByOrNull { args.drop(1).joinToString(" ").levenshtein(it.name) }
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getCharacters
                }
                sender.sendMessage(
                    plugin.messages.characterGroupViewTitle.withParameters(
                        character = character
                    )
                )
                for (group in groupService.getGroups(character).join()) {
                    val message = plugin.messages.groupViewItem.withParameters(group = group)
                    val messageComponents = mutableListOf<BaseComponent>()
                    var chatColor: ChatColor? = null
                    var chatFormat: ChatColor? = null
                    var messageBuffer = StringBuilder()
                    var i = 0
                    while (i < message.length) {
                        if (message[i] == ChatColor.COLOR_CHAR) {
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
                            reorderButton.hoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ComponentBuilder("Click to switch ${group.name.value}'s priority with another group").create()
                            )
                            reorderButton.clickEvent = ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/charactergroup prepareswitchpriority ${profile.name + profile.discriminator} ${character.name} ${group.name.value}"
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
