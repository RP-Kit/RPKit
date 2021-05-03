package com.rpkit.permissions.bukkit.command.group

import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GroupViewCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.group.view")) {
            sender.sendMessage(plugin.messages.noPermissionGroupView)
            return true
        }

        val player = if (args.isNotEmpty()) {
            val target = plugin.server.getPlayer(args[0])
            if (target != null) {
                target
            } else {
                sender.sendMessage(plugin.messages.groupViewInvalidPlayer)
                return true
            }
        } else {
            sender
        }

        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return true
        }

        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }

        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileSelf)
            return true
        }

        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfile)
            return true
        }

        sender.sendMessage(plugin.messages.groupViewTitle.withParameters(
            profile = profile
        ))
        for (group in groupService.getGroups(profile)) {
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
                            ChatColor.of("#${message[i + 2]}${message[i + 4]}${message[i + 6]}${message[i + 8]}${message[i + 10]}${message[i + 12]}")
                        i += 13
                    } else {
                        val colorOrFormat = ChatColor.getByChar(message[i + 1])
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
                        i += 2
                    }
                } else if (message.substring(i, (i + "\${reorder}".length).coerceAtMost(message.length)) == "\${reorder}") {
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
                    reorderButton.hoverEvent = HoverEvent(SHOW_TEXT, listOf(Text("Click to switch ${group.name.value}'s priority with another group")))
                    reorderButton.clickEvent = ClickEvent(RUN_COMMAND, "/group prepareswitchpriority ${minecraftProfile.minecraftUsername.value} ${group.name.value}")
                    messageComponents.add(reorderButton)
                    i += "\${reorder}".length
                } else {
                    messageBuffer.append(message[i++])
                }
            }
            appendComponent(messageComponents, messageBuffer, chatColor, chatFormat)
            sender.spigot().sendMessage(*messageComponents.toTypedArray())
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
