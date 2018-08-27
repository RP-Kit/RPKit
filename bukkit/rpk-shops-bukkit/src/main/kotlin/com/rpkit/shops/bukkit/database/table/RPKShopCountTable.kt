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

package com.rpkit.shops.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.database.jooq.rpkit.Tables.RPKIT_SHOP_COUNT
import com.rpkit.shops.bukkit.shopcount.RPKShopCount
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the shop count table.
 */
class RPKShopCountTable(database: Database, private val plugin: RPKShopsBukkit): Table<RPKShopCount>(database, RPKShopCount::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_shop_count.id.enabled")) {
        database.cacheManager.createCache("rpk-shops-bukkit.rpkit_shop_count.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKShopCount::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_shop_count.id.size"))).build())
    } else {
        null
    }

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_shop_count.character_id.enabled")) {
        database.cacheManager.createCache("rpk-shops-bukkit.rpkit_shop_count.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_shop_count.character_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_SHOP_COUNT)
                .column(RPKIT_SHOP_COUNT.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_SHOP_COUNT.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_SHOP_COUNT.COUNT, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_shop_count").primaryKey(RPKIT_SHOP_COUNT.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKShopCount): Int {
        database.create
                .insertInto(
                        RPKIT_SHOP_COUNT,
                        RPKIT_SHOP_COUNT.CHARACTER_ID,
                        RPKIT_SHOP_COUNT.COUNT
                )
                .values(
                        entity.character.id,
                        entity.count
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        characterCache?.put(entity.character.id, id)
        return id
    }

    override fun update(entity: RPKShopCount) {
        database.create
                .update(RPKIT_SHOP_COUNT)
                .set(RPKIT_SHOP_COUNT.CHARACTER_ID, entity.character.id)
                .set(RPKIT_SHOP_COUNT.COUNT, entity.count)
                .where(RPKIT_SHOP_COUNT.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        characterCache?.put(entity.character.id, entity.id)
    }

    override fun get(id: Int): RPKShopCount? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_SHOP_COUNT.CHARACTER_ID,
                            RPKIT_SHOP_COUNT.COUNT
                    )
                    .from(RPKIT_SHOP_COUNT)
                    .where(RPKIT_SHOP_COUNT.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_SHOP_COUNT.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (character != null) {
                val shopCount = RPKShopCount(
                        id,
                        character,
                        result.get(RPKIT_SHOP_COUNT.COUNT)
                )
                cache?.put(id, shopCount)
                characterCache?.put(shopCount.character.id, id)
                return shopCount
            } else {
                database.create
                        .deleteFrom(RPKIT_SHOP_COUNT)
                        .where(RPKIT_SHOP_COUNT.ID.eq(id))
                        .execute()
                characterCache?.remove(characterId)
                return null
            }
        }
    }

    /**
     * Gets the shop count for a character.
     * If there is no shop count for the given character
     *
     * @param character The character
     * @return The shop count for the character, or null if there is no shop count for the given character
     */
    fun get(character: RPKCharacter): RPKShopCount? {
        if (characterCache?.containsKey(character.id) == true) {
            return get(characterCache.get(character.id))
        } else {
            val result = database.create
                    .select(RPKIT_SHOP_COUNT.ID)
                    .from(RPKIT_SHOP_COUNT)
                    .where(RPKIT_SHOP_COUNT.CHARACTER_ID.eq(character.id))
                    .fetchOne() ?: return null
            return get(result.get(RPKIT_SHOP_COUNT.ID))
        }
    }

    override fun delete(entity: RPKShopCount) {
        database.create
                .deleteFrom(RPKIT_SHOP_COUNT)
                .where(RPKIT_SHOP_COUNT.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        characterCache?.remove(entity.character.id)
    }
}