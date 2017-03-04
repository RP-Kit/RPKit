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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Item consume listener for checking expiry dates.
 */
class PlayerItemConsumeListener(private val plugin: RPKFoodBukkit): Listener {

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val item = event.item
        val expiryProvider = plugin.core.serviceManager.getServiceProvider(RPKExpiryProviderImpl::class)
        if (item.type.isEdible) {
            if (expiryProvider.isExpired(item)) {
                event.player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 1200, 0))
                event.player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 300, 0))
                event.player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 300, 0))
            }
        }
    }
}