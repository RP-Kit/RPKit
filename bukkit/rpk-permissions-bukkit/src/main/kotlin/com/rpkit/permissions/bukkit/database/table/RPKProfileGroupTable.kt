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

package com.rpkit.permissions.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.jooq.Tables.RPKIT_PROFILE_GROUP
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.RPKProfileGroup
import com.rpkit.players.bukkit.profile.RPKProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKProfileGroupTable(private val database: Database, private val plugin: RPKPermissionsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_profile_group.profile_id.enabled")) {
        database.cacheManager.createCache("rpk-permissions-bukkit.rpkit_profile_group.profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Map::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_profile_group.profile_id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKProfileGroup) {
        database.create
                .insertInto(
                        RPKIT_PROFILE_GROUP,
                        RPKIT_PROFILE_GROUP.PROFILE_ID,
                        RPKIT_PROFILE_GROUP.GROUP_NAME,
                        RPKIT_PROFILE_GROUP.PRIORITY
                )
                .values(
                        entity.profile.id,
                        entity.group.name,
                        entity.priority
                )
                .execute()
        val groupMap = cache?.get(entity.profile.id) as? MutableMap<String, RPKProfileGroup> ?: mutableMapOf()
        groupMap[entity.group.name] = entity
        cache?.put(entity.profile.id, groupMap)
    }

    fun update(entity: RPKProfileGroup) {
        database.create
                .update(RPKIT_PROFILE_GROUP)
                .set(RPKIT_PROFILE_GROUP.PRIORITY, entity.priority)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(entity.profile.id))
                .and(RPKIT_PROFILE_GROUP.GROUP_NAME.eq(entity.group.name))
                .execute()
        val groupMap = cache?.get(entity.profile.id) as? MutableMap<String, RPKProfileGroup> ?: mutableMapOf()
        groupMap[entity.group.name] = entity
        cache?.put(entity.profile.id, groupMap)
    }

    operator fun get(profile: RPKProfile, group: RPKGroup): RPKProfileGroup? {
        if (cache?.containsKey(profile.id) == true) {
            val groupMap = cache[profile.id] as? MutableMap<String, RPKProfileGroup> ?: mutableMapOf()
            if (groupMap.contains(group.name)) {
                return groupMap[group.name]
            }
        }
        val result = database.create
                .select(
                        RPKIT_PROFILE_GROUP.PRIORITY
                )
                .from(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profile.id))
                .and(RPKIT_PROFILE_GROUP.GROUP_NAME.eq(group.name))
                .fetchOne() ?: return null
        val profileGroup = RPKProfileGroup(
                profile,
                group,
                result[RPKIT_PROFILE_GROUP.PRIORITY]
        )
        val groupMap = cache?.get(profileGroup.profile.id) as? MutableMap<String, RPKProfileGroup> ?: mutableMapOf()
        groupMap[profileGroup.group.name] = profileGroup
        cache?.put(profileGroup.profile.id, groupMap)
        return profileGroup
    }

    fun get(profile: RPKProfile): List<RPKProfileGroup> = database.create
            .select(
                    RPKIT_PROFILE_GROUP.GROUP_NAME,
                    RPKIT_PROFILE_GROUP.PRIORITY
            )
            .from(RPKIT_PROFILE_GROUP)
            .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profile.id))
            .orderBy(RPKIT_PROFILE_GROUP.PRIORITY.desc())
            .fetch()
            .mapNotNull { result ->
                val group = result[RPKIT_PROFILE_GROUP.GROUP_NAME]
                        .let { Services[RPKGroupService::class]?.getGroup(it) }
                        ?: return@mapNotNull null
                RPKProfileGroup(
                        profile,
                        group,
                        result[RPKIT_PROFILE_GROUP.PRIORITY]
                )
            }

    fun delete(entity: RPKProfileGroup) {
        database.create
                .deleteFrom(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(entity.profile.id))
                .and(RPKIT_PROFILE_GROUP.GROUP_NAME.eq(entity.group.name))
                .execute()
        val groupMap = cache?.get(entity.profile.id) as? MutableMap<String, RPKProfileGroup> ?: mutableMapOf()
        groupMap.remove(entity.group.name)
        cache?.put(entity.profile.id, groupMap)
    }

}