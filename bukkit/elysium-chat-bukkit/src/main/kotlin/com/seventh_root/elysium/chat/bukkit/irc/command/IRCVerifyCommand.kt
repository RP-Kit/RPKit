package com.seventh_root.elysium.chat.bukkit.irc.command

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import org.pircbotx.Channel
import org.pircbotx.User


class IRCVerifyCommand(private val plugin: ElysiumChatBukkit): IRCCommand("verify") {

    override fun execute(channel: Channel, sender: User, cmd: IRCCommand, label: String, args: Array<String>) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class.java)
        if (args.size > 0) {
            ircProvider.ircBot.sendIRC().message("NickServ", "VERIFY REGISTER " + ircProvider.ircBot.nick + " " + args[0])
            sender.send().message(plugin.config.getString("messages.irc-verify-valid"))
        } else {
            sender.send().message(plugin.config.getString("messages.irc-verify-invalid-verification-code-not-specified"))
        }
    }

}