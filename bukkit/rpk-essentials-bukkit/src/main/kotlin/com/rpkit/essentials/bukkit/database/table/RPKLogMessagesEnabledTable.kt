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

package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.jooq.Tables.RPKIT_LOG_MESSAGES_ENABLED
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessagesEnabled
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKLogMessagesEnabledTable(private val database: Database, plugin: RPKEssentialsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_log_messages_enabled.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache("rpk-essentials-bukkit.rpkit_log_messages_enabled.minecraft_profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKLogMessagesEnabled::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_log_messages_enabled.minecraft_profile_id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKLogMessagesEnabled) {
        database.create
                .insertInto(
                        RPKIT_LOG_MESSAGES_ENABLED,
                        RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID
                )
                .values(
                        entity.minecraftProfile.id
                )
                .execute()
        cache?.put(entity.minecraftProfile.id, entity)
    }

    fun update(entity: RPKLogMessagesEnabled) {
        database.create
                .update(RPKIT_LOG_MESSAGES_ENABLED)
                .set(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.put(entity.minecraftProfile.id, entity)
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): RPKLogMessagesEnabled? {
        if (cache?.containsKey(minecraftProfile.id) == true) {
            return cache[minecraftProfile.id]
        }
        database.create
                .select(
                        RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID
                )
                .from(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        val logMessagesEnabled = RPKLogMessagesEnabled(minecraftProfile)
        cache?.put(minecraftProfile.id, logMessagesEnabled)
        return logMessagesEnabled
    }

    fun delete(entity: RPKLogMessagesEnabled) {
        database.create
                .deleteFrom(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(entity.minecraftProfile.id))
                .execute()
        cache?.remove(entity.minecraftProfile.id)
    }

}