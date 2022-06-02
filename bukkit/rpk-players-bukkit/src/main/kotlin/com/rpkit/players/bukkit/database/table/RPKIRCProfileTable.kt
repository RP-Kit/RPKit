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
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_IRC_PROFILE
import com.rpkit.players.bukkit.profile.*
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileId
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileImpl
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKIRCProfileTable(
    private val database: Database,
    private val plugin: RPKPlayersBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_irc_profile.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_irc_profile.id",
            Int::class.javaObjectType,
            RPKIRCProfile::class.java,
            plugin.config.getLong("caching.rpkit_irc_profile.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKIRCProfile): CompletableFuture<Void> {
        val profile = entity.profile
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_IRC_PROFILE,
                    RPKIT_IRC_PROFILE.PROFILE_ID,
                    RPKIT_IRC_PROFILE.NICK
                )
                .values(
                    if (profile is RPKProfile) {
                        profile.id?.value
                    } else {
                        null
                    },
                    entity.nick.value
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKIRCProfileId(id)
            cache?.set(id, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert IRC profile", exception)
            throw exception
        }
    }

    fun update(entity: RPKIRCProfile): CompletableFuture<Void> {
        val profile = entity.profile
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_IRC_PROFILE)
                .set(
                    RPKIT_IRC_PROFILE.PROFILE_ID,
                    if (profile is RPKProfile) {
                        profile.id?.value
                    } else {
                        null
                    }
                )
                .set(RPKIT_IRC_PROFILE.NICK, entity.nick.value)
                .where(RPKIT_IRC_PROFILE.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update IRC profile", exception)
            throw exception
        }
    }

    operator fun get(id: RPKIRCProfileId): CompletableFuture<out RPKIRCProfile?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_IRC_PROFILE.PROFILE_ID,
                    RPKIT_IRC_PROFILE.NICK
                )
                .from(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.ID.eq(id.value))
                .fetchOne() ?: return@supplyAsync null
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync null
            val profileId = result.get(RPKIT_IRC_PROFILE.PROFILE_ID)
            val profile = if (profileId != null) {
                profileService.getProfile(RPKProfileId(profileId)).join()
            } else {
                null
            } ?: RPKThinProfileImpl(RPKProfileName(result.get(RPKIT_IRC_PROFILE.NICK)))
            val ircProfile = RPKIRCProfileImpl(
                id,
                profile,
                RPKIRCNick(result.get(RPKIT_IRC_PROFILE.NICK))
            )
            cache?.set(id.value, ircProfile)
            return@supplyAsync ircProfile
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get IRC profile", exception)
            throw exception
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKIRCProfile>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_IRC_PROFILE.ID)
                .from(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.PROFILE_ID.eq(profileId.value))
                .fetch()
            val ircProfileFutures = results.map { result ->
                get(RPKIRCProfileId(result.get(RPKIT_IRC_PROFILE.ID)))
            }
            CompletableFuture.allOf(*ircProfileFutures.toTypedArray()).join()
            return@supplyAsync ircProfileFutures.mapNotNull(CompletableFuture<out RPKIRCProfile?>::join)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get IRC profile", exception)
            throw exception
        }
    }

    fun get(nick: RPKIRCNick): CompletableFuture<RPKIRCProfile?> {
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_IRC_PROFILE.ID)
                .from(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.NICK.eq(nick.value))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync get(RPKIRCProfileId(result.get(RPKIT_IRC_PROFILE.ID))).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get IRC profile", exception)
            throw exception
        }
    }

    fun delete(entity: RPKIRCProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete IRC profile", exception)
            throw exception
        }
    }

}