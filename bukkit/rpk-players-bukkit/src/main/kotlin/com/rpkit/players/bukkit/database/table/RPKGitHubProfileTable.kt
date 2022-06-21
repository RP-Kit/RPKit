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
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_GITHUB_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfile
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileId
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileImpl
import com.rpkit.players.bukkit.profile.github.RPKGitHubUsername
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKGitHubProfileTable(
    private val database: Database,
    private val plugin: RPKPlayersBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_github_profile.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_github_profile.id",
            Int::class.javaObjectType,
            RPKGitHubProfile::class.java,
            plugin.config.getLong("caching.rpkit_github_profile.id.size")
        )
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_github_profile.name.enabled")) {
        database.cacheManager.createCache(
            "rpk-players-bukkit.rpkit_github_profile.name",
            String::class.java,
            RPKGitHubProfile::class.java,
            plugin.config.getLong("caching.rpkit_github_profile.name.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKGitHubProfile): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_GITHUB_PROFILE,
                    RPKIT_GITHUB_PROFILE.PROFILE_ID,
                    RPKIT_GITHUB_PROFILE.NAME,
                    RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                )
                .values(
                    profileId.value,
                    entity.name.value,
                    entity.oauthToken
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKGitHubProfileId(id)
            cache?.set(id, entity)
            nameCache?.set(entity.name.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert github profile", exception)
            throw exception
        }
    }

    fun update(entity: RPKGitHubProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_GITHUB_PROFILE)
                .set(RPKIT_GITHUB_PROFILE.PROFILE_ID, profileId.value)
                .set(RPKIT_GITHUB_PROFILE.NAME, entity.name.value)
                .set(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN, entity.oauthToken)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(id.value))
                .execute()
            cache?.set(id.value, entity)
            nameCache?.set(entity.name.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update github profile", exception)
            throw exception
        }
    }

    operator fun get(id: RPKGitHubProfileId): CompletableFuture<out RPKGitHubProfile?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_GITHUB_PROFILE.PROFILE_ID,
                    RPKIT_GITHUB_PROFILE.NAME,
                    RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                )
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(id.value))
                .fetchOne() ?: return@supplyAsync null
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync null
            val profileId = result.get(RPKIT_GITHUB_PROFILE.PROFILE_ID)
            val profile = profileService.getProfile(RPKProfileId(profileId)).join()
            if (profile != null) {
                val githubProfile = RPKGitHubProfileImpl(
                    id,
                    profile,
                    RPKGitHubUsername(result.get(RPKIT_GITHUB_PROFILE.NAME)),
                    result.get(RPKIT_GITHUB_PROFILE.OAUTH_TOKEN)
                )
                cache?.set(id.value, githubProfile)
                nameCache?.set(githubProfile.name.value, githubProfile)
                return@supplyAsync githubProfile
            } else {
                database.create
                    .deleteFrom(RPKIT_GITHUB_PROFILE)
                    .where(RPKIT_GITHUB_PROFILE.ID.eq(id.value))
                    .execute()
                return@supplyAsync null
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get github profile", exception)
            throw exception
        }
    }

    operator fun get(name: RPKGitHubUsername): CompletableFuture<out RPKGitHubProfile?> {
        if (nameCache?.containsKey(name.value) == true) {
            return CompletableFuture.completedFuture(nameCache[name.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_GITHUB_PROFILE.ID,
                    RPKIT_GITHUB_PROFILE.PROFILE_ID,
                    RPKIT_GITHUB_PROFILE.OAUTH_TOKEN
                )
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.NAME.eq(name.value))
                .fetchOne() ?: return@supplyAsync null
            val id = result[RPKIT_GITHUB_PROFILE.ID]
            val profileService = Services[RPKProfileService::class.java] ?: return@supplyAsync null
            val profileId = result.get(RPKIT_GITHUB_PROFILE.PROFILE_ID)
            val profile = profileService.getProfile(RPKProfileId(profileId)).join()
            if (profile != null) {
                val githubProfile = RPKGitHubProfileImpl(
                    RPKGitHubProfileId(id),
                    profile,
                    name,
                    result[RPKIT_GITHUB_PROFILE.OAUTH_TOKEN]
                )
                cache?.set(id, githubProfile)
                nameCache?.set(name.value, githubProfile)
                return@supplyAsync githubProfile
            } else {
                database.create
                    .deleteFrom(RPKIT_GITHUB_PROFILE)
                    .where(RPKIT_GITHUB_PROFILE.ID.eq(id))
                    .execute()
                cache?.remove(id)
                nameCache?.remove(name.value)
                return@supplyAsync null
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get github profile", exception)
            throw exception
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKGitHubProfile>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_GITHUB_PROFILE.ID)
                .from(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.PROFILE_ID.eq(profileId.value))
                .fetch()
            val githubProfileFutures = results.map { result ->
                get(RPKGitHubProfileId(result.get(RPKIT_GITHUB_PROFILE.ID)))
            }
            CompletableFuture.allOf(*githubProfileFutures.toTypedArray()).join()
            return@supplyAsync githubProfileFutures.mapNotNull(CompletableFuture<out RPKGitHubProfile?>::join)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get github profiles", exception)
            throw exception
        }
    }

    fun delete(entity: RPKGitHubProfile): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_GITHUB_PROFILE)
                .where(RPKIT_GITHUB_PROFILE.ID.eq(id.value))
                .execute()
            cache?.remove(id.value)
            nameCache?.remove(entity.name.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete github profile", exception)
            throw exception
        }
    }

}