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

package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_MINECRAFT_PROFILE_LINK_REQUEST
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileLinkRequest
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileLinkRequestImpl
import com.rpkit.players.bukkit.profile.RPKProfileService

class RPKMinecraftProfileLinkRequestTable(
        private val database: Database
) : Table {

    fun insert(entity: RPKMinecraftProfileLinkRequest) {
        database.create
                .insertInto(
                        RPKIT_MINECRAFT_PROFILE_LINK_REQUEST,
                        RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID,
                        RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.profile.id,
                        entity.minecraftProfile.id
                )
                .execute()
    }

    fun get(minecraftProfile: RPKMinecraftProfile): List<RPKMinecraftProfileLinkRequest> {
        val results = database.create
                .select(
                        RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID,
                        RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID
                )
                .from(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST)
                .where(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetch()
        val profileService = Services[RPKProfileService::class] ?: return emptyList()
        return results.mapNotNull { result ->
            val profile = profileService.getProfile(result[RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID])
                    ?: return@mapNotNull null
            RPKMinecraftProfileLinkRequestImpl(
                profile,
                minecraftProfile
            )
        }
    }

    fun delete(entity: RPKMinecraftProfileLinkRequest) {
        database.create
                .deleteFrom(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST)
                .where(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID.eq(entity.profile.id))
                .and(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
    }
}