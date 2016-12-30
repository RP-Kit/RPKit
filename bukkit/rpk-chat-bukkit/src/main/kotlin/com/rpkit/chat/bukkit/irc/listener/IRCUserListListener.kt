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

package com.rpkit.chat.bukkit.irc.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.UserListEvent

/**
 * IRC user list listener.
 * Registers all users in channels with the IRC provider upon receiving the user list.
 */
class IRCUserListListener(private val plugin: RPKChatBukkit): ListenerAdapter() {

    override fun onUserList(event: UserListEvent) {
        val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
        event.users.forEach { user -> ircProvider.addIRCUser(user) }
    }

}