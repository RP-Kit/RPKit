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

package com.rpkit.craftingskill.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.addLore
import com.rpkit.core.service.Services
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import kotlin.math.ceil
import kotlin.math.roundToInt


class PrepareItemCraftListener(private val plugin: RPKCraftingSkillBukkit) : Listener {

    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        val bukkitPlayer = event.viewers.firstOrNull() as? Player
        if (bukkitPlayer == null) {
            event.inventory.result = null
            return
        }
        if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            event.inventory.result = null
            return
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            event.inventory.result = null
            return
        }
        val craftingSkillService = Services[RPKCraftingSkillService::class.java]
        if (craftingSkillService == null) {
            event.inventory.result = null
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            event.inventory.result = null
            return
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            event.inventory.result = null
            return
        }
        val material = event.inventory.result?.type
        if (material == null) {
            event.inventory.result = null
            return
        }
        val craftingSkill = craftingSkillService.getCraftingExperience(character, RPKCraftingAction.CRAFT, material)
        val quality = craftingSkillService.getQualityFor(RPKCraftingAction.CRAFT, material, craftingSkill)
        val amount = craftingSkillService.getAmountFor(RPKCraftingAction.CRAFT, material, craftingSkill)
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