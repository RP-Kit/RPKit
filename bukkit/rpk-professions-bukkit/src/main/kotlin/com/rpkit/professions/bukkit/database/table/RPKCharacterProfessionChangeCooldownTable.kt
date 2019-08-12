/*
 * Copyright 2019 Ren Binden
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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.jooq.rpkit.Tables.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN
import com.rpkit.professions.bukkit.profession.RPKCharacterProfessionChangeCooldown
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import java.sql.Timestamp

class RPKCharacterProfessionChangeCooldownTable(
        database: Database,
        val plugin: RPKProfessionsBukkit
): Table<RPKCharacterProfessionChangeCooldown>(
        database,
        RPKCharacterProfessionChangeCooldown::class
) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_profession_change_cooldown.id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_character_profession_change_cooldown.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterProfessionChangeCooldown::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_profession_change_cooldown.id.size"))).build())
    } else {
        null
    }

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_character_profession_change_cooldown.character_id.enabled")) {
        database.cacheManager.createCache("rpk-professions-bukkit.rpkit_character_profession_change_cooldown.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterProfessionChangeCooldown::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_profession_change_cooldown.character_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .column(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME, SQLDataType.TIMESTAMP)
                .constraints(
                        constraint("pk_rpkit_character_profession_change_cooldown").primaryKey(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.7.0")
        }
    }

    override fun insert(entity: RPKCharacterProfessionChangeCooldown): Int {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN,
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME
                )
                .values(
                        entity.character.id,
                        Timestamp.valueOf(entity.cooldownEndTime)
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        characterCache?.put(entity.character.id, entity)
        return id
    }

    override fun update(entity: RPKCharacterProfessionChangeCooldown) {
        database.create
                .update(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .set(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME, Timestamp.valueOf(entity.cooldownEndTime))
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        characterCache?.put(entity.character.id, entity)
    }

    override fun get(id: Int): RPKCharacterProfessionChangeCooldown? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID,
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME
                )
                .from(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID.eq(id))
                .fetchOne() ?: return null
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID])
        if (character == null) {
            database.create
                    .deleteFrom(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                    .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID.eq(id))
                    .execute()
            cache?.remove(id)
            characterCache?.remove(result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID])
            return null
        }
        val characterProfessionChangeCooldown = RPKCharacterProfessionChangeCooldown(
                id,
                character,
                result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME].toLocalDateTime()
        )
        cache?.put(id, characterProfessionChangeCooldown)
        characterCache?.put(characterProfessionChangeCooldown.character.id, characterProfessionChangeCooldown)
        return characterProfessionChangeCooldown
    }

    fun get(character: RPKCharacter): RPKCharacterProfessionChangeCooldown? {
        if (characterCache?.containsKey(character.id) == true) {
            return characterCache[character.id]
        }
        val result = database.create
                .select(
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID,
                        RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME
                )
                .from(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        val characterProfessionChangeCooldown = RPKCharacterProfessionChangeCooldown(
                result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID],
                character,
                result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME].toLocalDateTime()
        )
        cache?.put(result[RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID], characterProfessionChangeCooldown)
        characterCache?.put(character.id, characterProfessionChangeCooldown)
        return characterProfessionChangeCooldown
    }

    override fun delete(entity: RPKCharacterProfessionChangeCooldown) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN)
                .where(RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        characterCache?.remove(entity.character.id)
    }

}
