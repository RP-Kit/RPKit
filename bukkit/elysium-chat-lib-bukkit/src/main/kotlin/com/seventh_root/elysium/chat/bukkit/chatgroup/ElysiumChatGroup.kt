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

package com.seventh_root.elysium.chat.bukkit.chatgroup

import com.seventh_root.elysium.core.database.Entity
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Represents a chat group.
 */
interface ElysiumChatGroup: Entity {

    /**
     * The name of the chat group.
     */
    val name: String

    /**
     * A list of all members of the chat group.
     * This list is immutable, members must be added and removed with [addMember] and [removeMember], respectively.
     */
    val members: List<ElysiumPlayer>

    /**
     * A list of all people that have received an invitation to the chat group.
     * This list is immutable, invitations must be added and removed with [invite] and [uninvite], respectively.
     */
    val invited: List<ElysiumPlayer>

    /**
     * Adds a member to the chat group.
     *
     * @param player The player to add
     */
    fun addMember(player: ElysiumPlayer)

    /**
     * Removes a member from the chat group.
     *
     * @param player The player to remove
     */
    fun removeMember(player: ElysiumPlayer)

    /**
     * Invites a player to the chat group.
     *
     * @param player The player to invite
     */
    fun invite(player: ElysiumPlayer)

    /**
     * Uninvites a player from the chat group.
     *
     * @param player The player to uninvite
     */
    fun uninvite(player: ElysiumPlayer)

    /**
     * Sends a message to the chat group.
     *
     * @param sender The player sending the message
     * @param message The message
     */
    fun sendMessage(sender: ElysiumPlayer, message: String)
}