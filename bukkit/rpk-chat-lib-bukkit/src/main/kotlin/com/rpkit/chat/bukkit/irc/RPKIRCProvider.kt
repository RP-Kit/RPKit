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

package com.rpkit.chat.bukkit.irc

import com.rpkit.core.service.ServiceProvider
import org.pircbotx.PircBotX
import org.pircbotx.User

/**
 * Provides IRC related operations.
 */
interface RPKIRCProvider: ServiceProvider {

    /**
     * The IRC bot instance. May be used to send messages to the IRC server, among other operations.
     */
    val ircBot: PircBotX

    /**
     * Gets the IRC user with the given nickname.
     * If no user has the given nickname, null is returned.
     *
     * @param nick The nickname of the user
     * @return The IRC user with the given nickname
     */
    fun getIRCUser(nick: String): User?

    /**
     * Adds an IRC user to be tracked by this IRC provider.
     *
     * @param user The user to add
     */
    fun addIRCUser(user: User)

}