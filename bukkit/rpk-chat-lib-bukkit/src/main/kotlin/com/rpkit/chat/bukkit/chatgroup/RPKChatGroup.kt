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

package com.rpkit.chat.bukkit.chatgroup

import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture

/**
 * Represents a chat group.
 */
interface RPKChatGroup {

    /**
     * The ID of the chat group.
     * Guaranteed to be unique.
     * Null if not yet inserted into the database.
     */
    var id: RPKChatGroupId?

    /**
     * The name of the chat group.
     */
    val name: RPKChatGroupName

    /**
     * A list of all members of the chat group.
     * This list is immutable. members must be added and removed with [addMember] and [removeMember], respectively
     */
    val members: CompletableFuture<List<RPKMinecraftProfile>>

    /**
     * A list of all people that have received an invitation to the chat group.
     * This list is immutable, invitations must be added and removed with [invite] and [uninvite], respectively.
     */
    val invited: CompletableFuture<List<RPKMinecraftProfile>>

    /**
     * Adds a member to the chat group.
     *
     * @param minecraftProfile The Minecraft profile to add
     */
    fun addMember(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void>

    /**
     * Removes a member from the chat group
     *
     * @param minecraftProfile The Minecraft profile to remove
     */
    fun removeMember(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void>


    /**
     * Invites a Minecraft profile to the chat group.
     *
     * @param minecraftProfile The Minecraft profile to invite
     */
    fun invite(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void>

    /**
     * Uninvites a Minecraft profile from the chat group.
     *
     * @param minecraftProfile The Minecraft profile to uninvite
     */
    fun uninvite(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void>

    /**
     * Sends a message to the chat group.
     *
     * @param sender The Minecraft profile sending the message
     * @param message The message
     */
    fun sendMessage(sender: RPKMinecraftProfile, message: String): CompletableFuture<Void>

}