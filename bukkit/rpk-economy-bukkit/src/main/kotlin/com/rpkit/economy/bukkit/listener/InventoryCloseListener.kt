/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Inventory close listener for wallets.
 */
class InventoryCloseListener : Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!event.view.title.toLowerCase().contains("wallet")) return
        val bukkitPlayer = event.player
        if (bukkitPlayer !is Player) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val currencyService = Services[RPKCurrencyService::class.java] ?: return
        val economyService = Services[RPKEconomyService::class.java] ?: return
        val currency = currencyService.getCurrency(RPKCurrencyName(event.view.title.substringAfterLast("[").substringBeforeLast("]")))
                ?: return
        val amount = event.inventory.contents
                .filter { item ->
                    item != null && item.isSimilar(currency.item)
                }
                .map { item -> item.amount }
                .sum()
        event.inventory.contents
                .filter { item ->
                    item != null && !item.isSimilar(currency.item)
                }
                .forEach { item ->
                    bukkitPlayer.world.dropItem(bukkitPlayer.location, item)
                }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer) ?: return
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return
        economyService.setBalance(character, currency, amount)
    }

}
