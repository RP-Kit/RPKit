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

package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.util.addLore
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKCraftingAction
import com.rpkit.professions.bukkit.profession.RPKProfessionProvider
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import kotlin.math.ceil
import kotlin.math.roundToInt


class PrepareItemCraftListener(private val plugin: RPKProfessionsBukkit): Listener {

    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        val bukkitPlayer = event.viewers.firstOrNull() as? Player
        if (bukkitPlayer == null) {
            event.inventory.result = null
            return
        }
        if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            event.inventory.result = null
            return
        }
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        if (character == null) {
            event.inventory.result = null
            return
        }
        val material = event.inventory.result?.type
        if (material == null) {
            event.inventory.result = null
            return
        }
        val professions = professionProvider.getProfessions(character)
        val professionLevels = professions
                .associateWith { profession -> professionProvider.getProfessionLevel(character, profession) }
        val amount = professionLevels.entries
                .map { (profession, level) -> profession.getAmountFor(RPKCraftingAction.CRAFT, material, level) }
                .max() ?: plugin.config.getDouble("default.crafting.$material.amount", 1.0)
        val potentialQualities = professionLevels.entries
                .mapNotNull { (profession, level) -> profession.getQualityFor(RPKCraftingAction.CRAFT, material, level) }
        val quality = potentialQualities.maxBy(RPKItemQuality::durabilityModifier)
        val item = event.inventory.result ?: return
        if (quality != null) {
            item.addLore(quality.lore)
        }
        if (amount > 1) {
            item.amount = amount.roundToInt()
        } else if (amount < 1) {
            item.amount = ceil(amount).toInt()
        }
        event.inventory.result = item
    }

}