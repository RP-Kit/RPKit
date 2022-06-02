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

package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_MINECRAFT_PROFILE_LINK_REQUEST
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileLinkRequest
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileLinkRequestImpl
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class RPKMinecraftProfileLinkRequestTable(
        private val database: Database,
        private val plugin: RPKPlayersBukkit
) : Table {

    fun insert(entity: RPKMinecraftProfileLinkRequest): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_MINECRAFT_PROFILE_LINK_REQUEST,
                    RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID,
                    RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID
                )
                .values(
                    profileId.value,
                    minecraftProfileId.value
                )
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert Minecraft profile link request", exception)
            throw exception
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): CompletableFuture<out List<RPKMinecraftProfileLinkRequest>> {
        val minecraftProfileId = minecraftProfile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(
                    RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID,
                    RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID
                )
                .from(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST)
                .where(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetch()
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync emptyList()
            return@supplyAsync results.mapNotNull { result ->
                val profile =
                    profileService.getProfile(RPKProfileId(result[RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID]))
                        .join() ?: return@mapNotNull null
                RPKMinecraftProfileLinkRequestImpl(
                    profile,
                    minecraftProfile
                )
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get Minecraft profile link requests", exception)
            throw exception
        }
    }

    fun delete(entity: RPKMinecraftProfileLinkRequest): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val minecraftProfileId = entity.minecraftProfile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST)
                .where(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.PROFILE_ID.eq(profileId.value))
                .and(RPKIT_MINECRAFT_PROFILE_LINK_REQUEST.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete Minecraft profile link request", exception)
            throw exception
        }
    }
}