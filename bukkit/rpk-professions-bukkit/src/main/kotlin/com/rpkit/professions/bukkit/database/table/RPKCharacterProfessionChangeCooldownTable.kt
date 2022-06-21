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

package com.rpkit.professions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.create
import com.rpkit.professions.bukkit.database.jooq.Tables.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN
import com.rpkit.professions.bukkit.profession.RPKCharacterProfessionChangeCooldown
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class RPKCharacterProfessionChangeCooldownTable(
        private val database: Database,
        val plugin: RPKProfessionsBukkit
) : Table {

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_character_profession_change_cooldown.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-professions-bukkit.rpkit_character_profession_change_cooldown.character_id",
            Int::class.javaObjectType,
            RPKCharacterProfessionChangeCooldown::class.java,
            plugin.config.getLong("caching.rpkit_character_profession_change_cooldown.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCharacterProfessionChangeCooldown): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN,
                    RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID,
                    RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME
                )
                .values(
                    characterId.value,
                    entity.cooldownEndTime
                )
                .execute()
            characterCache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert character profession change cooldown", exception)
            throw exception
        }
    }

    fun update(entity: RPKCharacterProfessionChangeCooldown): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .set(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME, entity.cooldownEndTime)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .execute()
            characterCache?.set(characterId.value, entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update character profession change cooldown", exception)
            throw exception
        }
    }

    fun get(character: RPKCharacter): CompletableFuture<RPKCharacterProfessionChangeCooldown?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        if (characterCache?.containsKey(characterId.value) == true) {
            return CompletableFuture.completedFuture(characterCache[characterId.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME)
                .from(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .fetchOne() ?: return@supplyAsync null
            val characterProfessionChangeCooldown = RPKCharacterProfessionChangeCooldown(
                character,
                result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME]
            )
            characterCache?.set(characterId.value, characterProfessionChangeCooldown)
            return@supplyAsync characterProfessionChangeCooldown
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get character profession change cooldown", exception)
            throw exception
        }
    }

    fun delete(entity: RPKCharacterProfessionChangeCooldown): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .execute()
            characterCache?.remove(characterId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete character profession change cooldown", exception)
            throw exception
        }
    }

}
