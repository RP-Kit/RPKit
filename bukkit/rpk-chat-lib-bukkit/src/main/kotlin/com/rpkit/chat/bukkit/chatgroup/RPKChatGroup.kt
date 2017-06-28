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

package com.rpkit.chat.bukkit.chatgroup

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile

/**
 * Represents a chat group.
 */
interface RPKChatGroup: Entity {

    /**
     * The name of the chat group.
     */
    val name: String

    /**
     * A list of all members of the chat group.
     * This list is immutable, members must be added and removed with [addMember] and [removeMember], respectively.
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("memberMinecraftProfiles"))
    val members: List<RPKPlayer>

    /**
     * A list of all members of the chat group.
     * This list is immutable. members must be added and removed with [addMember] and [removeMember], respectively
     */
    val memberMinecraftProfiles: List<RPKMinecraftProfile>

    /**
     * A list of all people that have received an invitation to the chat group.
     * This list is immutable, invitations must be added and removed with [invite] and [uninvite], respectively.
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("invitedMinecraftProfiles"))
    val invited: List<RPKPlayer>

    /**
     * A list of all people that have received an invitation to the chat group.
     * This list is immutable, invitations must be added and removed with [invite] and [uninvite], respectively.
     */
    val invitedMinecraftProfiles: List<RPKMinecraftProfile>

    /**
     * Adds a member to the chat group.
     *
     * @param player The player to add
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("addMember(minecraftProfile)"))
    fun addMember(player: RPKPlayer)

    /**
     * Adds a member to the chat group.
     *
     * @param minecraftProfile The Minecraft profile to add
     */
    fun addMember(minecraftProfile: RPKMinecraftProfile)

    /**
     * Removes a member from the chat group.
     *
     * @param player The player to remove
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("removeMember(minecraftProfile)"))
    fun removeMember(player: RPKPlayer)

    /**
     * Removes a member from the chat group
     *
     * @param minecraftProfile The Minecraft profile to remove
     */
    fun removeMember(minecraftProfile: RPKMinecraftProfile)

    /**
     * Invites a player to the chat group.
     *
     * @param player The player to invite
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("invite(minecraftProfile)"))
    fun invite(player: RPKPlayer)

    /**
     * Invites a Minecraft profile to the chat group.
     *
     * @param minecraftProfile The Minecraft profile to invite
     */
    fun invite(minecraftProfile: RPKMinecraftProfile)

    /**
     * Uninvites a player from the chat group.
     *
     * @param player The player to uninvite
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("uninvite(minecraftProfile)"))
    fun uninvite(player: RPKPlayer)

    /**
     * Uninvites a Minecraft profile from the chat group.
     *
     * @param minecraftProfile The Minecraft profile to uninvite
     */
    fun uninvite(minecraftProfile: RPKMinecraftProfile)

    /**
     * Sends a message to the chat group.
     *
     * @param sender The player sending the message
     * @param message The message
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("sendMessage(sender, message)"))
    fun sendMessage(sender: RPKPlayer, message: String)

    /**
     * Sends a message to the chat group.
     *
     * @param sender The Minecraft profile sending the message
     * @param message The message
     */
    fun sendMessage(sender: RPKMinecraftProfile, message: String)

}