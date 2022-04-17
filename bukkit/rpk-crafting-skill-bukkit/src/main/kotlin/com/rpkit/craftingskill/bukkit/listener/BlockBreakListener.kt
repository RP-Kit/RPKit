/*
 * Copyright 2022 Ren Binden
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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random


class BlockBreakListener(private val plugin: RPKCraftingSkillBukkit) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val bukkitPlayer = event.player
        if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val craftingSkillService = Services[RPKCraftingSkillService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        event.isDropItems = false
        for (item in event.block.getDrops(event.player.inventory.itemInMainHand)) {
            val material = item.type
            craftingSkillService.getCraftingExperience(character, RPKCraftingAction.MINE, material)
                .thenAccept { craftingSkill ->
                    val quality = craftingSkillService.getQualityFor(RPKCraftingAction.MINE, material, craftingSkill)
                    val amount = craftingSkillService.getAmountFor(RPKCraftingAction.MINE, material, craftingSkill)
                    if (quality != null) {
                        item.addLore(quality.lore)
                    }
                    var dropItem = false
                    if (amount > 1) {
                        item.amount = amount.roundToInt()
                        dropItem = true
                    } else if (amount < 1) {
                        val random = Random.nextDouble()
                        if (random <= amount) {
                            item.amount = 1
                            dropItem = true
                        }
                    } else {
                        dropItem = true
                    }
                    val maxExperience = plugin.config.getConfigurationSection("mining.$material")
                        ?.getKeys(false)
                        ?.maxOfOrNull(String::toInt)
                        ?: 0
                    if (maxExperience != 0 && craftingSkill < maxExperience) {
                        val totalExperience = min(craftingSkill + item.amount, maxExperience)
                        craftingSkillService.setCraftingExperience(
                            character,
                            RPKCraftingAction.MINE,
                            material,
                            totalExperience
                        ).thenRun {
                            event.player.sendMessage(
                                plugin.messages["mine-experience", mapOf(
                                    "total_experience" to totalExperience.toString(),
                                    "received_experience" to item.amount.toString()
                                )]
                            )
                        }
                    }
                    if (dropItem) {
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            event.block.world.dropItemNaturally(event.block.location, item)
                        })
                    }
                }
        }
    }

}