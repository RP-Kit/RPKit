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

package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_IRC_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileId
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileImpl


class RPKIRCProfileTable(private val database: Database, plugin: RPKPlayersBukkit) : Table {

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

    fun insert(entity: RPKIRCProfile) {
        val profile = entity.profile
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
    }

    fun update(entity: RPKIRCProfile) {
        val profile = entity.profile
        val id = entity.id ?: return
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
    }

    operator fun get(id: RPKIRCProfileId): RPKIRCProfile? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        }
        val result = database.create
                .select(
                        RPKIT_IRC_PROFILE.PROFILE_ID,
                        RPKIT_IRC_PROFILE.NICK
                )
                .from(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.ID.eq(id.value))
                .fetchOne() ?: return null
        val profileService = Services[RPKProfileService::class.java] ?: return null
        val profileId = result.get(RPKIT_IRC_PROFILE.PROFILE_ID)
        val profile = if (profileId != null) {
            profileService.getProfile(RPKProfileId(profileId))
        } else {
            null
        } ?: RPKThinProfileImpl(RPKProfileName(result.get(RPKIT_IRC_PROFILE.NICK)))
        val ircProfile = RPKIRCProfileImpl(
                id,
                profile,
                RPKIRCNick(result.get(RPKIT_IRC_PROFILE.NICK))
        )
        cache?.set(id.value, ircProfile)
        return ircProfile
    }

    fun get(profile: RPKProfile): List<RPKIRCProfile> {
        val profileId = profile.id ?: return emptyList()
        val results = database.create
                .select(RPKIT_IRC_PROFILE.ID)
                .from(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.PROFILE_ID.eq(profileId.value))
                .fetch()
        return results.map { result ->
            get(RPKIRCProfileId(result.get(RPKIT_IRC_PROFILE.ID)))
        }.filterNotNull()
    }

    fun get(nick: RPKIRCNick): RPKIRCProfile? {
        val result = database.create
                .select(RPKIT_IRC_PROFILE.ID)
                .from(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.NICK.eq(nick.value))
                .fetchOne() ?: return null
        return get(RPKIRCProfileId(result.get(RPKIT_IRC_PROFILE.ID)))
    }

    fun delete(entity: RPKIRCProfile) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_IRC_PROFILE)
                .where(RPKIT_IRC_PROFILE.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }

}