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

package com.rpkit.locks.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.bukkit.extension.toByteArray
import com.rpkit.core.bukkit.extension.toItemStackArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.create
import com.rpkit.locks.bukkit.database.jooq.Tables.RPKIT_KEYRING
import com.rpkit.locks.bukkit.keyring.RPKKeyring
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level
import java.util.logging.Level.SEVERE


class RPKKeyringTable(private val database: Database, private val plugin: RPKLocksBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_keyring.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-locks-bukkit.rpkit_keyring.character_id",
            Int::class.javaObjectType,
            RPKKeyring::class.java,
            plugin.config.getLong("caching.rpkit_keyring.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKKeyring): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_KEYRING,
                    RPKIT_KEYRING.CHARACTER_ID,
                    RPKIT_KEYRING.ITEMS
                )
                .values(
                    characterId.value,
                    entity.items.toTypedArray().toByteArray()
                )
                .execute()
            cache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert keyring", exception)
            throw exception
        }
    }

    fun update(entity: RPKKeyring): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .update(RPKIT_KEYRING)
                .set(RPKIT_KEYRING.ITEMS, entity.items.toTypedArray().toByteArray())
                .where(RPKIT_KEYRING.CHARACTER_ID.eq(characterId.value))
                .execute()
            cache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update keyring", exception)
            throw exception
        }
    }

    operator fun get(character: RPKCharacter): CompletableFuture<RPKKeyring?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        if (cache?.containsKey(characterId.value) == true) {
            return CompletableFuture.completedFuture(cache[characterId.value])
        } else {
            return CompletableFuture.supplyAsync {
                val result = database.create
                    .select(
                        RPKIT_KEYRING.CHARACTER_ID,
                        RPKIT_KEYRING.ITEMS
                    )
                    .from(RPKIT_KEYRING)
                    .where(RPKIT_KEYRING.CHARACTER_ID.eq(characterId.value))
                    .fetchOne() ?: return@supplyAsync null
                val keyring = RPKKeyring(
                    character,
                    result.get(RPKIT_KEYRING.ITEMS).toItemStackArray().toMutableList()
                )
                cache?.set(characterId.value, keyring)
                return@supplyAsync keyring
            }.exceptionally { exception ->
                plugin.logger.log(Level.SEVERE, "Failed to get keyring", exception)
                throw exception
            }
        }
    }

    fun delete(entity: RPKKeyring): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return runAsync {
            database.create
                .deleteFrom(RPKIT_KEYRING)
                .where(RPKIT_KEYRING.CHARACTER_ID.eq(characterId.value))
                .execute()
            cache?.remove(characterId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete keyring", exception)
            throw exception
        }
    }

    fun delete(characterId: RPKCharacterId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_KEYRING)
            .where(RPKIT_KEYRING.CHARACTER_ID.eq(characterId.value))
            .execute()
        cache?.remove(characterId.value)
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete keyring for character id", exception)
        throw exception
    }
}