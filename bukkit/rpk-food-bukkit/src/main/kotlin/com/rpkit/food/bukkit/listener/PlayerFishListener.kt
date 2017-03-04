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
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent

/**
 * Player fish listener for adding expiry dates.
 */
class PlayerFishListener(private val plugin: RPKFoodBukkit): Listener {

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        val caught = event.caught
        if (caught != null) {
            if (caught is Item) {
                val item = caught.itemStack
                val expiryProvider = plugin.core.serviceManager.getServiceProvider(RPKExpiryProviderImpl::class)
                expiryProvider.setExpiry(item)
                caught.itemStack = item
            }
        }
    }
}
