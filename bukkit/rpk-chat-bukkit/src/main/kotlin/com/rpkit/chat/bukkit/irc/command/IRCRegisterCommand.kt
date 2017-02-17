/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.chat.bukkit.irc.command

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import org.pircbotx.Channel
import org.pircbotx.User

/**
 * IRC register command.
 * Registers the IRC bot with NickServ on the server using the password given in the config, and the e-mail specified.
 */
class IRCRegisterCommand(private val plugin: RPKChatBukkit): IRCCommand("register") {

    override fun execute(channel: Channel, sender: User, cmd: IRCCommand, label: String, args: Array<String>) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
        if (args.isNotEmpty()) {
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