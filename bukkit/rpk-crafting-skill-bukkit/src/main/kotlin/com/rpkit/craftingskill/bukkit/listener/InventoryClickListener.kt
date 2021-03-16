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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.FurnaceInventory
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random


class InventoryClickListener(private val plugin: RPKCraftingSkillBukkit) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory !is FurnaceInventory) return
        if (event.slotType != InventoryType.SlotType.RESULT) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val craftingSkillService = Services[RPKCraftingSkillService::class.java] ?: return
        val bukkitPlayer = event.whoClicked as? Player ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        val item = event.currentItem ?: return
        if (item.amount == 0 || item.type == Material.AIR) return
        val material = item.type
        val craftingSkill = craftingSkillService.getCraftingExperience(character, RPKCraftingAction.SMELT, material)
        val quality = craftingSkillService.getQualityFor(RPKCraftingAction.SMELT, material, craftingSkill)
        val amount = craftingSkillService.getAmountFor(RPKCraftingAction.SMELT, material, craftingSkill) * item.amount
        if (quality != null) {
            item.addLore(quality.lore)
        }
        if (amount > 1) {
            item.amount = amount.roundToInt()
        } else if (amount < 1) {
            val random = Random.nextDouble()
            if (random <= amount) {
                item.amount = 1
            } else {
                event.currentItem = null
                return
            }
        }
        event.currentItem = item
        val maxExperience = plugin.config.getConfigurationSection("smelting.$material")
            ?.getKeys(false)
            ?.map(String::toInt)
            ?.maxOrNull()
            ?: 0
        if (maxExperience != 0 && craftingSkill < maxExperience) {
            val totalExperience = min(craftingSkill + item.amount, maxExperience)
            craftingSkillService.setCraftingExperience(character, RPKCraftingAction.SMELT, item.type, totalExperience)
            event.whoClicked.sendMessage(plugin.messages["smelt-experience", mapOf(
                "total_experience" to totalExperience.toString(),
                "received_experience" to item.amount.toString()
            )])
        }
    }

}