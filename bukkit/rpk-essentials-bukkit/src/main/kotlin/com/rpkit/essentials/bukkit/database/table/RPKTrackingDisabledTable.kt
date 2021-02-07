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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.create
import com.rpkit.essentials.bukkit.database.jooq.Tables.RPKIT_TRACKING_DISABLED
import com.rpkit.essentials.bukkit.tracking.RPKTrackingDisabled


class RPKTrackingDisabledTable(private val database: Database, private val plugin: RPKEssentialsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_tracking_enabled.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-essentials-bukkit.rpkit_tracking_enabled.character_id",
            Int::class.javaObjectType,
            RPKTrackingDisabled::class.java,
            plugin.config.getLong("caching.rpkit_tracking_enabled.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKTrackingDisabled) {
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_TRACKING_DISABLED,
                        RPKIT_TRACKING_DISABLED.CHARACTER_ID
                )
                .values(
                    characterId.value
                )
                .execute()
        cache?.set(characterId.value, entity)
    }

    operator fun get(character: RPKCharacter): RPKTrackingDisabled? {
        val characterId = character.id ?: return null
        if (cache?.containsKey(characterId.value) == true) {
            return cache[characterId.value]
        }
        database.create
                .select(
                        RPKIT_TRACKING_DISABLED.CHARACTER_ID
                )
                .from(RPKIT_TRACKING_DISABLED)
                .where(RPKIT_TRACKING_DISABLED.CHARACTER_ID.eq(characterId.value))
                .fetchOne() ?: return null
        val trackingEnabled = RPKTrackingDisabled(character)
        cache?.set(characterId.value, trackingEnabled)
        return trackingEnabled
    }

    fun delete(entity: RPKTrackingDisabled) {
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_TRACKING_DISABLED)
                .where(RPKIT_TRACKING_DISABLED.CHARACTER_ID.eq(characterId.value))
                .execute()
        cache?.remove(characterId.value)
    }
}