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

package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.create
import com.rpkit.essentials.bukkit.database.jooq.Tables.RPKIT_LOG_MESSAGES_ENABLED
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessagesEnabled
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile


class RPKLogMessagesEnabledTable(private val database: Database, plugin: RPKEssentialsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_log_messages_enabled.minecraft_profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-essentials-bukkit.rpkit_log_messages_enabled.minecraft_profile_id",
            Int::class.javaObjectType,
            RPKLogMessagesEnabled::class.java,
            plugin.config.getLong("caching.rpkit_log_messages_enabled.minecraft_profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKLogMessagesEnabled) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .insertInto(
                    RPKIT_LOG_MESSAGES_ENABLED,
                    RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID
                )
                .values(
                    minecraftProfileId.value
                )
                .execute()
        cache?.set(minecraftProfileId.value, entity)
    }

    operator fun get(minecraftProfile: RPKMinecraftProfile): RPKLogMessagesEnabled? {
        val minecraftProfileId = minecraftProfile.id ?: return null
        if (cache?.containsKey(minecraftProfileId.value) == true) {
            return cache[minecraftProfileId.value]
        }
        database.create
                .select(
                        RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID
                )
                .from(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .fetchOne() ?: return null
        val logMessagesEnabled = RPKLogMessagesEnabled(minecraftProfile)
        cache?.set(minecraftProfileId.value, logMessagesEnabled)
        return logMessagesEnabled
    }

    fun delete(entity: RPKLogMessagesEnabled) {
        val minecraftProfileId = entity.minecraftProfile.id ?: return
        database.create
                .deleteFrom(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        cache?.remove(minecraftProfileId.value)
    }

}