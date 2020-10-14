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

package com.rpkit.professions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.jooq.Tables.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN
import com.rpkit.professions.bukkit.profession.RPKCharacterProfessionChangeCooldown
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class RPKCharacterProfessionChangeCooldownTable(
        private val database: Database,
        val plugin: RPKProfessionsBukkit
) : Table {

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_character_profession_change_cooldown.character_id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_character_profession_change_cooldown.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterProfessionChangeCooldown::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_profession_change_cooldown.character_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKCharacterProfessionChangeCooldown) {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN,
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME
                )
                .values(
                        entity.character.id,
                        entity.cooldownEndTime
                )
                .execute()
        characterCache?.put(entity.character.id, entity)
    }

    fun update(entity: RPKCharacterProfessionChangeCooldown) {
        database.create
                .update(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .set(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME, entity.cooldownEndTime)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(entity.character.id))
                .execute()
        characterCache?.put(entity.character.id, entity)
    }

    fun get(character: RPKCharacter): RPKCharacterProfessionChangeCooldown? {
        if (characterCache?.containsKey(character.id) == true) {
            return characterCache[character.id]
        }
        val result = database.create
                .select(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME)
                .from(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        val characterProfessionChangeCooldown = RPKCharacterProfessionChangeCooldown(
                character,
                result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME]
        )
        characterCache?.put(character.id, characterProfessionChangeCooldown)
        return characterProfessionChangeCooldown
    }

    fun delete(entity: RPKCharacterProfessionChangeCooldown) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(entity.character.id))
                .execute()
        characterCache?.remove(entity.character.id)
    }

}
