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

package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.create
import com.rpkit.characters.bukkit.database.jooq.Tables.RPKIT_NEW_CHARACTER_COOLDOWN
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldown
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKNewCharacterCooldownTable(private val database: Database, private val plugin: RPKCharactersBukkit) : Table {

    private val profileCache = if (plugin.config.getBoolean("caching.rpkit_new_character_cooldown.profile_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-characters-bukkit.rpkit_new_character_cooldown.profile_id",
            Int::class.javaObjectType,
            RPKNewCharacterCooldown::class.java,
            plugin.config.getLong("caching.rpkit_new_character_cooldown.profile_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKNewCharacterCooldown): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_NEW_CHARACTER_COOLDOWN,
                    RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID,
                    RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .values(
                    profileId.value,
                    entity.cooldownExpiryTime
                )
                .execute()
            profileCache?.set(profileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert new character cooldown", exception)
            throw exception
        }
    }

    fun update(entity: RPKNewCharacterCooldown): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_NEW_CHARACTER_COOLDOWN)
                .set(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP, entity.cooldownExpiryTime)
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(profileId.value))
                .execute()
            profileCache?.set(profileId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update new character cooldown", exception)
            throw exception
        }
    }

    operator fun get(profile: RPKProfile): CompletableFuture<RPKNewCharacterCooldown?> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(null)
        if (profileCache?.containsKey(profileId.value) == true) {
            return CompletableFuture.completedFuture(profileCache[profileId.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID,
                    RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .from(RPKIT_NEW_CHARACTER_COOLDOWN)
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(profileId.value))
                .fetchOne() ?: return@supplyAsync null
            val newCharacterCooldown = RPKNewCharacterCooldown(
                profile,
                result.get(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP)
            )
            profileCache?.set(profileId.value, newCharacterCooldown)
            return@supplyAsync newCharacterCooldown
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get new character cooldown", exception)
            throw exception
        }
    }

    fun delete(entity: RPKNewCharacterCooldown): CompletableFuture<Void> {
        val profileId = entity.profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_NEW_CHARACTER_COOLDOWN)
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(profileId.value))
                .execute()
            profileCache?.remove(profileId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete new character cooldown", exception)
            throw exception
        }
    }

}