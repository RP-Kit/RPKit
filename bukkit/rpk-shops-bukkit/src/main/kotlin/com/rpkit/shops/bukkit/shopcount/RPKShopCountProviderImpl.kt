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

/**
 * Shop count service implementation.
 */
class RPKShopCountServiceImpl(override val plugin: RPKShopsBukkit) : RPKShopCountService {

    override fun getShopCount(character: RPKCharacter): Int {
        return plugin.database.getTable(RPKShopCountTable::class).get(character)?.count ?: 0
    }

    override fun setShopCount(character: RPKCharacter, amount: Int) {
        val shopCount = plugin.database.getTable(RPKShopCountTable::class).get(character)
                ?: RPKShopCount(character, 0)
        shopCount.count = amount
        plugin.database.getTable(RPKShopCountTable::class).update(shopCount)
    }

}
