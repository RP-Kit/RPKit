/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.chat.bukkit.irc.listener

import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.irc.IRCNick
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.QuitEvent

class IRCChannelQuitListener : ListenerAdapter() {

    override fun onQuit(event: QuitEvent) {
        val ircService = Services[RPKIRCService::class.java] ?: return
        val user = event.user ?: return
        ircService.setOnline(IRCNick(user.nick), false)
    }

}