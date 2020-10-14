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
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_GITHUB_PROFILE
import com.rpkit.players.bukkit.profile.RPKGitHubProfile
import com.rpkit.players.bukkit.profile.RPKGitHubProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.kohsuke.github.GHUser


class RPKGitHubProfileTable(private val database: Database, plugin: RPKPlayersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_github_profile.id.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_github_profile.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKGitHubProfile::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_github_profile.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKGitHubProfile) {
        database.create
                .insertInto(
                        RPKIT_GITHUB_PROFILE,
                        RPKIT_GITHUB_PROFILE.PROFILE_ID,
                        RPKIT_GITHUB_PROFILE.NAME,
                        RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                )
                .values(
                        entity.profile.id,
                        entity.name,
                        entity.oauthToken
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKGitHubProfile) {
        database.create
                .update(RPKIT_GITHUB_PROFILE)
                .set(RPKIT_GITHUB_PROFILE.PROFILE_ID, entity.profile.id)
                .set(RPKIT_GITHUB_PROFILE.NAME, entity.name)
                .set(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN, entity.oauthToken)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKGitHubProfile? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        }
        val result = database.create
                .select(
                        RPKIT_GITHUB_PROFILE.PROFILE_ID,
                        RPKIT_GITHUB_PROFILE.NAME,
                        RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                )
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(id))
                .fetchOne() ?: return null
        val profileService = Services[RPKProfileService::class] ?: return null
        val profileId = result.get(RPKIT_GITHUB_PROFILE.PROFILE_ID)
        val profile = profileService.getProfile(profileId)
        if (profile != null) {
            val githubProfile = RPKGitHubProfileImpl(
                    id,
                    profile,
                    result.get(RPKIT_GITHUB_PROFILE.NAME),
                    result.get(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN)
            )
            cache?.put(githubProfile.id, githubProfile)
            return githubProfile
        } else {
            database.create
                    .deleteFrom(RPKIT_GITHUB_PROFILE)
                    .where(RPKIT_GITHUB_PROFILE.ID.eq(id))
                    .execute()
            return null
        }
    }

    fun get(profile: RPKProfile): List<RPKGitHubProfile> {
        val results = database.create
                .select(RPKIT_GITHUB_PROFILE.ID)
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.PROFILE_ID.eq(profile.id))
                .fetch()
        return results.map { result ->
            get(result.get(RPKIT_GITHUB_PROFILE.ID))
        }.filterNotNull()
    }

    fun get(user: GHUser): RPKGitHubProfile? {
        val result = database.create
                .select(RPKIT_GITHUB_PROFILE.ID)
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.NAME.eq(user.name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_GITHUB_PROFILE.ID))
    }

    fun delete(entity: RPKGitHubProfile) {
        database.create
                .deleteFrom(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}