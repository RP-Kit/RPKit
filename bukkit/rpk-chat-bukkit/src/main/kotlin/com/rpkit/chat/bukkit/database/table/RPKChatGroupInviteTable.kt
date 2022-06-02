/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.chat.bukkit.database.table

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupId
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupInvite
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_GROUP_INVITE
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Represents chat group invite table.
 */
class RPKChatGroupInviteTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    fun insert(entity: RPKChatGroupInvite): CompletableFuture<Void> {
        val chatGroupId = entity.chatGroup.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CHAT_GROUP_INVITE,
                    RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID,
                    RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID
                )
                .values(
                    chatGroupId.value,
                    minecraftProfileId.value
                )
                .execute()
        }
    }

    /**
     * Gets a list of invites for a particular chat group.
     *
     * @param chatGroup The chat group
     * @return A list of chat group invites
     */
    fun get(chatGroup: RPKChatGroup): CompletableFuture<List<RPKChatGroupInvite>> {
        val chatGroupId = chatGroup.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            return@supplyAsync database.create
                .select(RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID)
                .from(RPKIT_CHAT_GROUP_INVITE)
                .where(RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID.eq(chatGroupId.value))
                .fetch()
                .mapNotNull { result ->
                    val minecraftProfileService =
                        Services[RPKMinecraftProfileService::class.java] ?: return@mapNotNull null
                    val minecraftProfile = minecraftProfileService
                        .getMinecraftProfile(RPKMinecraftProfileId(result[RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID]))
                        .join()
                        ?: return@mapNotNull null
                    return@mapNotNull RPKChatGroupInvite(
                        chatGroup,
                        minecraftProfile
                    )
                }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get chat group invites", exception)
            throw exception
        }
    }

    /**
     * Gets a list of chat group invites for a particular Minecraft profile.
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group invites for the Minecraft profile
     */
    fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<List<RPKChatGroupInvite>> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            return@supplyAsync database.create
                .select(RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID)
                .from(RPKIT_CHAT_GROUP_INVITE)
                .where(RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetch()
                .mapNotNull { result ->
                    val chatGroupService = Services[RPKChatGroupService::class.java] ?: return@mapNotNull null
                    val chatGroup = chatGroupService
                        .getChatGroup(RPKChatGroupId(result[RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID])).join()
                        ?: return@mapNotNull null
                    return@mapNotNull RPKChatGroupInvite(
                        chatGroup,
                        minecraftProfile
                    )
                }
        }
    }

    fun delete(entity: RPKChatGroupInvite): CompletableFuture<Void> {
        val chatGroupId = entity.chatGroup.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHAT_GROUP_INVITE)
                .where(RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID.eq(chatGroupId.value))
                .and(RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        }
    }

}