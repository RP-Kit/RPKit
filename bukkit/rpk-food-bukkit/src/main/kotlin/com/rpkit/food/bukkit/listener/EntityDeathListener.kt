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

package com.rpkit.food.bukkit.listener

import com.rpkit.food.bukkit.RPKFoodBukkit
import com.rpkit.food.bukkit.expiry.RPKExpiryProviderImpl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

/**
 * Entity death listener for setting expiry dates.
 */
class EntityDeathListener(private val plugin: RPKFoodBukkit): Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.entity is Player) return
        val expiryProvider = plugin.core.serviceManager.getServiceProvider(RPKExpiryProviderImpl::class)
        val newDrops = mutableListOf<ItemStack>()
        for (drop in event.drops) {
            if (drop.type.isEdible) {
                expiryProvider.setExpiry(drop)
            }
            newDrops.add(drop)
        }
        event.drops.clear()
        event.drops.addAll(newDrops)
    }

}