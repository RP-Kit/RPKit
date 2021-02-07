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

package com.rpkit.food.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.food.bukkit.expiry.RPKExpiryService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent


class InventoryOpenListener : Listener {

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val expiryService = Services[RPKExpiryService::class.java] ?: return
        event.inventory.contents
                .filterNotNull()
                .filter { it.type.isEdible }
                .forEach { item ->
                    if (expiryService.getExpiry(item) == null) {
                        expiryService.setExpiry(item)
                    }
                }
        event.player.inventory.contents
                .filterNotNull()
                .filter { it.type.isEdible }
                .forEach { item ->
                    if (expiryService.getExpiry(item) == null) {
                        expiryService.setExpiry(item)
                    }
                }
    }

}