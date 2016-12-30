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

package com.rpkit.food.bukkit.expiry

import com.rpkit.core.service.ServiceProvider
import com.rpkit.food.bukkit.RPKFoodBukkit
import org.bukkit.inventory.ItemStack
import java.text.SimpleDateFormat
import java.util.*


class ExpiryProvider(private val plugin: RPKFoodBukkit): ServiceProvider {

    val dateFormat = SimpleDateFormat(plugin.config.getString("date-format"))

    fun setExpiry(item: ItemStack, timestamp: Long) {
        if (!item.type.isEdible) return
        val itemMeta = item.itemMeta
        val lore = if (itemMeta.hasLore()) itemMeta.lore else mutableListOf<String>()
        lore.removeAll(lore.filter { it.startsWith("Expires: ") })
        val expiryDate = Date(timestamp)
        lore.add("Expires: ${dateFormat.format(expiryDate)}")
        itemMeta.lore = lore
        item.itemMeta = itemMeta
    }

    fun getExpiry(item: ItemStack): Long? {
        if (!item.type.isEdible) return null
        val itemMeta = item.itemMeta
        if (itemMeta.hasLore()) {
            val lore = itemMeta.lore
            val expiryLore = lore.filter { it.startsWith("Expires: ") }.firstOrNull()
            if (expiryLore != null) {
                val expiryDateString = expiryLore.drop("Expires: ".length)
                val expiryDate = dateFormat.parse(expiryDateString)
                return expiryDate.time
            }
        }
        return null
    }

    fun isExpired(item: ItemStack): Boolean {
        val expiryTimestamp = getExpiry(item) ?: return true
        return expiryTimestamp <= System.currentTimeMillis()
    }

}