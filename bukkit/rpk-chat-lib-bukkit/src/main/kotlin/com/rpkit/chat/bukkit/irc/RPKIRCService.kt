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

package com.rpkit.chat.bukkit.irc

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile

/**
 * Provides IRC related operations.
 */
interface RPKIRCService : Service {

    /**
     * Whether there is an active connection to IRC
     */
    val isConnected: Boolean

    /**
     * The nickname of the IRC bridge bot
     */
    val nick: RPKIRCNick

    /**
     * Sends a message to the given channel
     * @param channel The IRC channel to send a message to
     * @param message The message to send
     */
    fun sendMessage(channel: IRCChannel, message: String)

    /**
     * Sends a message to the given IRC nick
     * @param nick The nick of the user to send a message to
     * @param message The message to send
     */
    fun sendMessage(nick: RPKIRCNick, message: String)

    /**
     * Sends a message to the given IRC profile
     * @param user The user to send a message to
     * @param message The message to send
     */
    fun sendMessage(user: RPKIRCProfile, message: String)

    /**
     * Checks whether a user with the given nick is online
     *
     * @param nick The nickname to check
     * @return Whether the IRC profile is online
     */
    fun isOnline(nick: RPKIRCNick): Boolean

    /**
     * Sets whether a user with the given nick is online
     *
     * @param nick The nickname to set the online state of
     */
    fun setOnline(nick: RPKIRCNick, isOnline: Boolean)

    /**
     * Joins an IRC channel
     *
     * @param ircChannel The IRC channel to join
     */
    fun joinChannel(ircChannel: IRCChannel)

    /**
     * Connects to the IRC server
     */
    fun connect()

    /**
     * Disconnects from the IRC server
     */
    fun disconnect()

}