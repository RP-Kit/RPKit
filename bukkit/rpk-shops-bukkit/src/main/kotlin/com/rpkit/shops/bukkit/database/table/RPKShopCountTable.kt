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

package com.rpkit.shops.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.database.jooq.Tables.RPKIT_SHOP_COUNT
import com.rpkit.shops.bukkit.shopcount.RPKShopCount
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

/**
 * Represents the shop count table.
 */
class RPKShopCountTable(private val database: Database, private val plugin: RPKShopsBukkit) : Table {

    private val characterCache = if (plugin.config.getBoolean("caching.rpkit_shop_count.character_id.enabled")) {
        database.cacheManager.createCache("rpk-shops-bukkit.rpkit_shop_count.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKShopCount::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_shop_count.character_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKShopCount) {
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
        characterCache?.put(entity.character.id, entity)
    }

    fun update(entity: RPKShopCount) {
        database.create
                .update(RPKIT_SHOP_COUNT)
                .set(RPKIT_SHOP_COUNT.COUNT, entity.count)
                .where(RPKIT_SHOP_COUNT.CHARACTER_ID.eq(entity.character.id))
                .execute()
        characterCache?.put(entity.character.id, entity)
    }

    /**
     * Gets the shop count for a character.
     * If there is no shop count for the given character
     *
     * @param character The character
     * @return The shop count for the character, or null if there is no shop count for the given character
     */
    operator fun get(character: RPKCharacter): RPKShopCount? {
        if (characterCache?.containsKey(character.id) == true) {
            return characterCache.get(character.id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_SHOP_COUNT.CHARACTER_ID,
                            RPKIT_SHOP_COUNT.COUNT
                    )
                    .from(RPKIT_SHOP_COUNT)
                    .where(RPKIT_SHOP_COUNT.CHARACTER_ID.eq(character.id))
                    .fetchOne() ?: return null
            val shopCount = RPKShopCount(
                    character,
                    result.get(RPKIT_SHOP_COUNT.COUNT)
            )
            characterCache?.put(shopCount.character.id, shopCount)
            return shopCount
        }
    }

    fun delete(entity: RPKShopCount) {
        database.create
                .deleteFrom(RPKIT_SHOP_COUNT)
                .where(RPKIT_SHOP_COUNT.CHARACTER_ID.eq(entity.character.id))
                .execute()
        characterCache?.remove(entity.character.id)
    }
}