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
import com.rpkit.core.bukkit.util.addLore
import com.rpkit.core.service.Services
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random


class BlockBreakListener(private val plugin: RPKCraftingSkillBukkit) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val bukkitPlayer = event.player
        if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            event.isDropItems = false
            return
        }
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            event.isDropItems = false
            return
        }
        val craftingSkillService = Services[RPKCraftingSkillService::class]
        if (craftingSkillService == null) {
            event.isDropItems = false
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            event.isDropItems = false
            return
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            event.isDropItems = false
            return
        }
        val itemsToDrop = mutableListOf<ItemStack>()
        for (item in event.block.getDrops(event.player.inventory.itemInMainHand)) {
            val material = item.type
            val craftingSkill = craftingSkillService.getCraftingExperience(character, RPKCraftingAction.MINE, material)
            val quality = craftingSkillService.getQualityFor(RPKCraftingAction.MINE, material, craftingSkill)
            val amount = craftingSkillService.getAmountFor(RPKCraftingAction.MINE, material, craftingSkill)
            if (quality != null) {
                item.addLore(quality.lore)
            }
            if (amount > 1) {
                item.amount = amount.roundToInt()
                itemsToDrop.add(item)
            } else if (amount < 1) {
                val random = Random.nextDouble()
                if (random <= amount) {
                    item.amount = 1
                    itemsToDrop.add(item)
                }
            } else {
                itemsToDrop.add(item)
            }
            val maxExperience = plugin.config.getConfigurationSection("mining.$material")
                    ?.getKeys(false)
                    ?.map(String::toInt)
                    ?.max()
                    ?: 0
            if (maxExperience != 0 && craftingSkill < maxExperience) {
                val totalExperience = min(craftingSkill + item.amount, maxExperience)
                craftingSkillService.setCraftingExperience(character, RPKCraftingAction.MINE, material, totalExperience)
                event.player.sendMessage(plugin.messages["mine-experience", mapOf(
                        Pair("total-experience", totalExperience.toString()),
                        Pair("received-experience", item.amount.toString())
                )])
            }
        }
        event.isDropItems = false
        for (item in itemsToDrop) {
            event.block.world.dropItemNaturally(event.block.location, item)
        }
    }

}