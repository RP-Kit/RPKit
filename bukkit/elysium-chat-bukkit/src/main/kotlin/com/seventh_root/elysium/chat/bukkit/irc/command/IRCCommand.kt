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
