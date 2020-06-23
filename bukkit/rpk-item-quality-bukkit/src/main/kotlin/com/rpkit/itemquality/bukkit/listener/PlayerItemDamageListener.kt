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

package com.rpkit.itemquality.bukkit.listener

import com.rpkit.itemquality.bukkit.RPKItemQualityBukkit
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent


class PlayerItemDamageListener(private val plugin: RPKItemQualityBukkit): Listener {

    @EventHandler
    fun onPlayerItemDamage(event: PlayerItemDamageEvent) {
        val itemQualityProvider = plugin.core.serviceManager.getServiceProvider(RPKItemQualityProvider::class)
        val itemQuality = itemQualityProvider.getItemQuality(event.item) ?: return
        event.damage = Math.round(event.damage / itemQuality.durabilityModifier).toInt()
    }

}