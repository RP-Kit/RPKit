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

package com.rpkit.food.bukkit.expiry

import com.rpkit.food.bukkit.RPKFoodBukkit
import org.bukkit.inventory.ItemStack
import java.text.SimpleDateFormat
import java.util.*


class RPKExpiryServiceImpl(override val plugin: RPKFoodBukkit) : RPKExpiryService {

    val dateFormat = SimpleDateFormat(plugin.config.getString("date-format"))

    override fun setExpiry(item: ItemStack, expiryDate: Date) {
        if (!item.type.isEdible) return
        val itemMeta = item.itemMeta ?: plugin.server.itemFactory.getItemMeta(item.type) ?: return
        val lore = itemMeta.lore ?: mutableListOf<String>()
        lore.removeAll(lore.filter { it.startsWith("Expires: ") })
        lore.add("Expires: ${dateFormat.format(expiryDate)}")
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

    override fun setExpiry(item: ItemStack) {
        setExpiry(item, Date(System.currentTimeMillis() + (plugin.config.getLong("food-expiry.${item.type}",
                plugin.config.getLong("food-expiry.default")) * 1000)))
    }

    override fun getExpiry(item: ItemStack): Date? {
        if (!item.type.isEdible) return null
        val itemMeta = item.itemMeta ?: plugin.server.itemFactory.getItemMeta(item.type) ?: return null
        val lore = itemMeta.lore
        if (lore != null) {
            val expiryLore = lore.firstOrNull { it.startsWith("Expires: ") }
            if (expiryLore != null) {
                val expiryDateString = expiryLore.drop("Expires: ".length)
                return dateFormat.parse(expiryDateString)
            }
        }
        return null
    }

    override fun isExpired(item: ItemStack): Boolean {
        val expiryTimestamp = getExpiry(item) ?: return true
        return expiryTimestamp.time <= System.currentTimeMillis()
    }

}