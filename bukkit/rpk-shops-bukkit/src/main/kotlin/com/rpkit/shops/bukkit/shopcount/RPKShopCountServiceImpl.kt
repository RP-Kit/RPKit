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

package com.rpkit.shops.bukkit.shopcount

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.database.table.RPKShopCountTable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Shop count service implementation.
 */
class RPKShopCountServiceImpl(override val plugin: RPKShopsBukkit) : RPKShopCountService {

    private val shopCounts = ConcurrentHashMap<Int, Int>()

    override fun getShopCount(character: RPKCharacter): CompletableFuture<Int> {
        return plugin.database.getTable(RPKShopCountTable::class.java)[character].thenApply { it?.count ?: 0 }
    }

    override fun setShopCount(character: RPKCharacter, amount: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val existingShopCount = plugin.database.getTable(RPKShopCountTable::class.java)[character].join()
            if (existingShopCount != null) {
                existingShopCount.count = amount
                plugin.database.getTable(RPKShopCountTable::class.java).update(existingShopCount).join()
            } else {
                val shopCount = RPKShopCount(character, 0)
                plugin.database.getTable(RPKShopCountTable::class.java).insert(shopCount).join()
            }
            if (character.minecraftProfile?.isOnline == true) {
                val characterId = character.id
                if (characterId != null) {
                    shopCounts[characterId.value] = amount
                }
            }
        }
    }

    override fun getPreloadedShopCount(character: RPKCharacter): Int? {
        val characterId = character.id ?: return null
        return shopCounts[characterId.value]
    }

    override fun loadShopCount(character: RPKCharacter): CompletableFuture<Int> {
        val preloadedShopCount = getPreloadedShopCount(character)
        if (preloadedShopCount != null) return CompletableFuture.completedFuture(preloadedShopCount)
        val characterId = character.id ?: return CompletableFuture.completedFuture(0)
        plugin.logger.info("Loading shop count for character ${character.name} (${characterId.value})...")
        return CompletableFuture.supplyAsync {
            val shopCount = plugin.database.getTable(RPKShopCountTable::class.java)[character].join()?.count ?: 0
            shopCounts[characterId.value] = shopCount
            plugin.logger.info("Loaded shop count for character ${character.name} (${characterId.value}): $shopCount")
            return@supplyAsync shopCount
        }
    }

    override fun unloadShopCount(character: RPKCharacter) {
        val characterId = character.id ?: return
        shopCounts.remove(characterId.value)
        plugin.logger.info("Unloaded shop count for character ${character.name} (${characterId.value})")
    }

}
