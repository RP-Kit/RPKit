/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import org.pircbotx.Channel
import org.pircbotx.User

/**
 * IRC verify command.
 * Verifies the IRC bot with NickServ based on the password that was sent to the e-mail when the bot was registered.
 */
class IRCVerifyCommand(private val plugin: RPKChatBukkit) : IRCCommand("verify") {

    override fun execute(channel: Channel, sender: User, cmd: IRCCommand, label: String, args: Array<String>) {
        val ircService = Services[RPKIRCService::class.java]
        if (ircService == null) {
            sender.send().message(plugin.messages["irc-no-irc-service"])
            return
        }
        if (args.isEmpty()) {
            sender.send().message(plugin.messages["irc-verify-invalid-verification-code-not-specified"])
            return
        }
        ircService.sendMessage(
                RPKIRCNick("NickServ"),
                "VERIFY REGISTER " + ircService.nick.value + " " + args[0]
        )
        sender.send().message(plugin.messages["irc-verify-valid"])
    }

}