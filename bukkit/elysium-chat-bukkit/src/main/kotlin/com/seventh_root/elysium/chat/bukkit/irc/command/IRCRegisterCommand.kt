package com.seventh_root.elysium.chat.bukkit.irc.command

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import org.pircbotx.Channel
import org.pircbotx.User


class IRCRegisterCommand(private val plugin: ElysiumChatBukkit): IRCCommand("register") {

    override fun execute(channel: Channel, sender: User, cmd: IRCCommand, label: String, args: Array<String>) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class.java)
        if (args.size > 0) {
            if (args[0].matches(Regex("(\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3})"))) {
                ircProvider.ircBot.sendIRC().message("NickServ", "REGISTER " + plugin.config.getString("irc.password") + " " + args[0])
                sender.send().message(plugin.config.getString("messages.irc-register-valid"))
            } else {
                sender.send().message(plugin.config.getString("messages.irc-register-invalid-email-invalid"))
            }
        } else {
            sender.send().message(plugin.config.getString("messages.irc-register-invalid-email-not-specified"))
        }
    }

}