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

package com.rpkit.economy.bukkit.listener

import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import org.bukkit.Material.AIR
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(private val plugin: RPKEconomyBukkit): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title.toLowerCase().contains("wallet")) {
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val currency = currencyProvider.getCurrency(
                    event.view.title.substringAfterLast("[").substringBeforeLast("]")
            ) ?: return
            val item = event.cursor ?: return
            if (item.type == AIR) return
            if (item.type == currency.material
                    && item.hasItemMeta()
                    && item.itemMeta?.hasDisplayName() != false
                    && item.itemMeta?.displayName == currency.nameSingular
            ) return
            if (event.clickedInventory == event.view.topInventory) {
                event.isCancelled = true
            }
        }
    }

}