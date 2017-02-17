package com.rpkit.chat.bukkit.irc.command

import com.rpkit.chat.bukkit.RPKChatBukkit
import org.pircbotx.Channel
import org.pircbotx.User




class IRCListCommand(private val plugin: RPKChatBukkit): IRCCommand("list") {
    override fun execute(channel: Channel, sender: User, cmd: IRCCommand, label: String, args: Array<String>) {
        sender.send().message(plugin.config.getString("messages.irc-list-title")
                + plugin.server.onlinePlayers.map { it.name }.joinToString())
    }
}