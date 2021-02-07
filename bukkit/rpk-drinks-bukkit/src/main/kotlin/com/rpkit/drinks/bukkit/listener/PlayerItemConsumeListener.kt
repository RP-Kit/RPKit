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

package com.rpkit.drinks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.drink.bukkit.drink.RPKDrinkService
import com.rpkit.drink.bukkit.event.drink.RPKBukkitDrinkEvent
import com.rpkit.drinks.bukkit.RPKDrinksBukkit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent


class PlayerItemConsumeListener(private val plugin: RPKDrinksBukkit) : Listener {

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val drinkService = Services[RPKDrinkService::class.java] ?: return
        val drink = drinkService.getDrink(event.item) ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return
        val drinkEvent = RPKBukkitDrinkEvent(character, drink)
        plugin.server.pluginManager.callEvent(drinkEvent)
        if (drinkEvent.isCancelled) {
            event.isCancelled = true
            return
        }
        drinkService.setDrunkenness(drinkEvent.character, drinkService.getDrunkenness(drinkEvent.character) + drinkEvent.drink.drunkenness)
    }

}