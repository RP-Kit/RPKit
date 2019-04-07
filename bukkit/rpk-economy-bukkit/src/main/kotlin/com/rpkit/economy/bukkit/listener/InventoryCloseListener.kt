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

package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Inventory close listener for wallets.
 */
class InventoryCloseListener(private val plugin: RPKEconomyBukkit): Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.title.toLowerCase().contains("wallet")) {
            val bukkitPlayer = event.player
            if (bukkitPlayer is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                val currency = currencyProvider.getCurrency(event.inventory.title.substringAfterLast("[").substringBeforeLast("]"))
                if (currency != null) {
                    val amount = event.inventory.contents
                            .filter { item ->
                                item != null
                                        && item.type === currency.material
                                        && item.hasItemMeta()
                                        && item.itemMeta?.hasDisplayName() == true
                                        && item.itemMeta?.displayName == currency.nameSingular
                            }
                            .map { item -> item.amount }
                            .sum()
                    event.inventory.contents
                            .filter { item ->
                                item != null
                                        && (
                                            item.type !== currency.material
                                                || !item.hasItemMeta()
                                                || item.itemMeta?.hasDisplayName() == false
                                                || item.itemMeta?.displayName != currency.nameSingular
                                        )
                            }
                            .forEach { item ->
                                bukkitPlayer.world.dropItem(bukkitPlayer.location, item)
                            }
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                    if (minecraftProfile != null) {
                        val character = characterProvider.getActiveCharacter(minecraftProfile)
                        if (character != null) {
                            economyProvider.setBalance(character, currency, amount)
                        }
                    }
                }
            }
        }
    }

}
