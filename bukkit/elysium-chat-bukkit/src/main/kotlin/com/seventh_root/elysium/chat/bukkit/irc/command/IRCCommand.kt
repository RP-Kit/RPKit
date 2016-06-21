package com.seventh_root.elysium.chat.bukkit.irc.command

import org.pircbotx.Channel
import org.pircbotx.User
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

abstract class IRCCommand(val name: String) : ListenerAdapter() {

    override fun onMessage(event: MessageEvent) {
        if (event.message.startsWith("!$name ") || event.message.replace("!$name", "").isEmpty()) {
            var args = arrayOf<String>()
            if (event.message.contains(" ")) {
                args = event.message.split(Regex("\\s+")).subList(1, event.message.split(Regex("\\s+")).size).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            execute(event.channel, event.user!!, this, if (event.message.contains(" ")) event.message.split(" ".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0]
                    .replace("!", "") else event.message.replace("!", ""), args)
        }
    }

    abstract fun execute(channel: Channel, sender: User, cmd: IRCCommand, label: String, args: Array<String>)

}
