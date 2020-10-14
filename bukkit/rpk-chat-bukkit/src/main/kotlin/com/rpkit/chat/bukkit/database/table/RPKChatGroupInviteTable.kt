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

package com.rpkit.chat.bukkit.database.table

import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupInvite
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_GROUP_INVITE
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService

/**
 * Represents chat group invite table.
 */
class RPKChatGroupInviteTable(private val database: Database) : Table {

    fun insert(entity: RPKChatGroupInvite) {
        database.create
                .insertInto(
                        RPKIT_CHAT_GROUP_INVITE,
                        RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID,
                        RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.chatGroup.id,
                        entity.minecraftProfile.id
                )
                .execute()
    }

    /**
     * Gets a list of invites for a particular chat group.
     *
     * @param chatGroup The chat group
     * @return A list of chat group invites
     */
    fun get(chatGroup: RPKChatGroup): List<RPKChatGroupInvite> = database.create
            .select(RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID)
            .from(RPKIT_CHAT_GROUP_INVITE)
            .where(RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID.eq(chatGroup.id))
            .fetch()
            .mapNotNull { result ->
                val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return@mapNotNull null
                val minecraftProfile = minecraftProfileService
                        .getMinecraftProfile(result[RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID])
                        ?: return@mapNotNull null
                RPKChatGroupInvite(
                        chatGroup,
                        minecraftProfile
                )
            }

    /**
     * Gets a list of chat group invites for a particular Minecraft profile.
     *
     * @param minecraftProfile The Minecraft profile
     * @return A list of chat group invites for the Minecraft profile
     */
    fun get(minecraftProfile: RPKMinecraftProfile): List<RPKChatGroupInvite> = database.create
            .select(RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID)
            .from(RPKIT_CHAT_GROUP_INVITE)
            .where(RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
            .fetch()
            .mapNotNull { result ->
                val chatGroupService = Services[RPKChatGroupService::class] ?: return@mapNotNull null
                val chatGroup = chatGroupService
                        .getChatGroup(result[RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID])
                        ?: return@mapNotNull null
                RPKChatGroupInvite(
                        chatGroup,
                        minecraftProfile
                )
            }

    fun delete(entity: RPKChatGroupInvite) {
        database.create
                .deleteFrom(RPKIT_CHAT_GROUP_INVITE)
                .where(RPKIT_CHAT_GROUP_INVITE.CHAT_GROUP_ID.eq(entity.chatGroup.id))
                .and(RPKIT_CHAT_GROUP_INVITE.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
    }

}