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

package com.rpkit.permissions.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.create
import com.rpkit.permissions.bukkit.database.jooq.Tables.RPKIT_PROFILE_GROUP
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupName
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.RPKProfileGroup
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level
import java.util.logging.Level.SEVERE


class RPKProfileGroupTable(private val database: Database, private val plugin: RPKPermissionsBukkit) : Table {

    data class ProfileGroupCacheKey(
        val profileId: Int,
        val groupName: String
    )

    val cache = if (plugin.config.getBoolean("caching.rpkit_profile_group.profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-permissions-bukkit.rpkit_profile_group.profile_id",
            ProfileGroupCacheKey::class.java,
            RPKProfileGroup::class.java,
            plugin.config.getLong("caching.rpkit_profile_group.profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKProfileGroup): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val groupName = entity.group.name
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_PROFILE_GROUP,
                    RPKIT_PROFILE_GROUP.PROFILE_ID,
                    RPKIT_PROFILE_GROUP.GROUP_NAME,
                    RPKIT_PROFILE_GROUP.PRIORITY
                )
                .values(
                    profileId.value,
                    groupName.value,
                    entity.priority
                )
                .execute()
            cache?.set(ProfileGroupCacheKey(profileId.value, groupName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert profile group", exception)
            throw exception
        }
    }

    fun update(entity: RPKProfileGroup): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val groupName = entity.group.name
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_PROFILE_GROUP)
                .set(RPKIT_PROFILE_GROUP.PRIORITY, entity.priority)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profileId.value))
                .and(RPKIT_PROFILE_GROUP.GROUP_NAME.eq(groupName.value))
                .execute()
            cache?.set(ProfileGroupCacheKey(profileId.value, groupName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update profile group", exception)
            throw exception
        }
    }

    operator fun get(profile: RPKProfile, group: RPKGroup): CompletableFuture<RPKProfileGroup?> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(null)
        val groupName = group.name
        val cacheKey = ProfileGroupCacheKey(profileId.value, groupName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_PROFILE_GROUP.PRIORITY
                )
                .from(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profileId.value))
                .and(RPKIT_PROFILE_GROUP.GROUP_NAME.eq(groupName.value))
                .fetchOne() ?: return@supplyAsync null
            val profileGroup = RPKProfileGroup(
                profile,
                group,
                result[RPKIT_PROFILE_GROUP.PRIORITY]
            )
            cache?.set(cacheKey, profileGroup)
            return@supplyAsync profileGroup
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get profile group", exception)
            throw exception
        }
    }

    fun get(profile: RPKProfile): CompletableFuture<List<RPKProfileGroup>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            return@supplyAsync database.create
                .select(
                    RPKIT_PROFILE_GROUP.GROUP_NAME,
                    RPKIT_PROFILE_GROUP.PRIORITY
                )
                .from(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profileId.value))
                .orderBy(RPKIT_PROFILE_GROUP.PRIORITY.desc())
                .fetch()
                .mapNotNull { result ->
                    val group = result[RPKIT_PROFILE_GROUP.GROUP_NAME]
                        .let { Services[RPKGroupService::class.java]?.getGroup(RPKGroupName(it)) }
                        ?: return@mapNotNull null
                    RPKProfileGroup(
                        profile,
                        group,
                        result[RPKIT_PROFILE_GROUP.PRIORITY]
                    )
                }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get profile groups", exception)
            throw exception
        }
    }

    fun delete(entity: RPKProfileGroup): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        val groupName = entity.group.name
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profileId.value))
                .and(RPKIT_PROFILE_GROUP.GROUP_NAME.eq(entity.group.name.value))
                .execute()
            cache?.set(ProfileGroupCacheKey(profileId.value, groupName.value), entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete profile group", exception)
            throw exception
        }
    }

    fun delete(profileId: RPKProfileId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_PROFILE_GROUP)
            .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profileId.value))
            .execute()
        cache?.removeMatching { it.profile.id?.value == profileId.value }
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete profile groups for profile id", exception)
        throw exception
    }

}