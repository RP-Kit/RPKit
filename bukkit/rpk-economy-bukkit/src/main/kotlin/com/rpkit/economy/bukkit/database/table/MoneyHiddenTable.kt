/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.economy.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.character.MoneyHidden
import com.rpkit.economy.bukkit.database.jooq.rpkit.Tables.MONEY_HIDDEN
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType

/**
 * Represents the money hidden table.
 */
class MoneyHiddenTable(database: Database, private val plugin: RPKEconomyBukkit): Table<MoneyHidden>(database, MoneyHidden::class) {

    private val cache = if (plugin.config.getBoolean("caching.money_hidden.id.enabled")) {
        database.cacheManager.createCache("rpk-economy-bukkit.money_hidden.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MoneyHidden::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.money_hidden.id.size"))).build())
    } else {
        null
    }

    private val characterCache = if (plugin.config.getBoolean("caching.money_hidden.character_id.enabled")) {
        database.cacheManager.createCache("rpk-economy-bukkit.money_hidden.character_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.money_hidden.character_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(MONEY_HIDDEN)
                .column(MONEY_HIDDEN.ID, SQLDataType.INTEGER.identity(true))
                .column(MONEY_HIDDEN.CHARACTER_ID, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_money_hidden").primaryKey(MONEY_HIDDEN.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: MoneyHidden): Int {
        database.create
                .insertInto(
                        MONEY_HIDDEN,
                        MONEY_HIDDEN.CHARACTER_ID
                )
                .values(
                        entity.character.id
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        characterCache?.put(entity.character.id, id)
        return id
    }

    override fun update(entity: MoneyHidden) {
        database.create
                .update(MONEY_HIDDEN)
                .set(MONEY_HIDDEN.CHARACTER_ID, entity.character.id)
                .where(MONEY_HIDDEN.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        characterCache?.put(entity.character.id, entity.id)
    }

    override fun get(id: Int): MoneyHidden? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(MONEY_HIDDEN.CHARACTER_ID)
                    .from(MONEY_HIDDEN)
                    .where(MONEY_HIDDEN.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(MONEY_HIDDEN.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (character != null) {
                val moneyHidden = MoneyHidden(
                        id,
                        character
                )
                cache?.put(id, moneyHidden)
                characterCache?.put(moneyHidden.character.id, id)
                return moneyHidden
            } else {
                characterCache?.remove(characterId)
                database.create
                        .deleteFrom(MONEY_HIDDEN)
                        .where(MONEY_HIDDEN.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    /**
     * Gets the money hidden instance for a character.
     * If there is no money hidden row for the character, null is returned.
     *
     * @param character The character
     * @return The money hidden instance, or null if there is no money hidden instance for the character
     */
    fun get(character: RPKCharacter): MoneyHidden? {
        if (characterCache?.containsKey(character.id) == true) {
            return get(characterCache.get(character.id))
        } else {
            val result = database.create
                    .select(MONEY_HIDDEN.ID)
                    .from(MONEY_HIDDEN)
                    .where(MONEY_HIDDEN.CHARACTER_ID.eq(character.id))
                    .fetchOne() ?: return null
            return get(result.get(MONEY_HIDDEN.ID))
        }
    }

    override fun delete(entity: MoneyHidden) {
        database.create
                .deleteFrom(MONEY_HIDDEN)
                .where(MONEY_HIDDEN.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        characterCache?.remove(entity.character.id)
    }

}
