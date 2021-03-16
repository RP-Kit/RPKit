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

package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKCraftingAction
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import org.bukkit.GameMode
import org.bukkit.Material.AIR
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt
import kotlin.random.Random


class CraftItemListener(private val plugin: RPKProfessionsBukkit) : Listener {

    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val bukkitPlayer = event.whoClicked
        if (bukkitPlayer is Player) {
            if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
            val characterService = Services[RPKCharacterService::class.java] ?: return
            val professionService = Services[RPKProfessionService::class.java] ?: return
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile == null) {
                event.isCancelled = true
                bukkitPlayer.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
            if (character == null) {
                event.isCancelled = true
                bukkitPlayer.sendMessage(plugin.messages["no-character"])
                return
            }
            val itemType = event.recipe.result.type
            val professions = professionService.getProfessions(character)
            val professionLevels = professions
                    .associateWith { profession -> professionService.getProfessionLevel(character, profession) }
            var amountCrafted = getAmountCrafted(event)
            val amount = professionLevels.entries
                .map { (profession, level) -> profession.getAmountFor(RPKCraftingAction.CRAFT, itemType, level) }
                .maxOrNull() ?: plugin.config.getDouble("default.crafting.$itemType.amount", 1.0)
            if (amount > 1) {
                amountCrafted *= amount.roundToInt()
            } else if (amount < 1) {
                amountCrafted = (amountCrafted * amount).roundToInt()
                if (amountCrafted == 0) {
                    if (Random.nextDouble() <= amount) {
                        amountCrafted = 1
                    }
                }
            }
            val currentItem = event.currentItem
            val item = if (currentItem == null) {
                ItemStack(event.recipe.result)
            } else {
                ItemStack(currentItem)
            }
            item.amount = amountCrafted
            event.isCancelled = true
            if (event.isShiftClick) {
                if (amountCrafted > 0) {
                    event.whoClicked.inventory.addItem(item)
                    val matrixItems = event.inventory.matrix
                    val newMatrixItems = arrayOfNulls<ItemStack>(9)
                    for ((i, matrixItem) in matrixItems.withIndex()) {
                        if (matrixItem == null) {
                            continue
                        }
                        matrixItem.amount -= amountCrafted
                        if (matrixItem.amount <= 0) {
                            newMatrixItems[i] = null
                            event.inventory.result = null
                        } else {
                            newMatrixItems[i] = matrixItem
                        }
                    }
                }
            } else {
                if (amountCrafted > 0) {
                    event.currentItem = item
                    event.isCancelled = false
                } else {
                    event.currentItem = null
                    val matrixItems = event.inventory.matrix
                    val newMatrixItems = arrayOfNulls<ItemStack>(9)
                    for ((i, matrixItem) in matrixItems.withIndex()) {
                        if (matrixItem == null) {
                            continue
                        }
                        matrixItem.amount -= amountCrafted
                        if (matrixItem.amount <= 0) {
                            newMatrixItems[i] = null
                        } else {
                            newMatrixItems[i] = matrixItem
                        }
                    }
                    event.inventory.matrix = newMatrixItems
                }
            }
            professions.forEach { profession ->
                val receivedExperience = plugin.config.getInt("professions.${profession.name.value}.experience.items.crafting.$itemType", 0) * amountCrafted
                if (receivedExperience > 0) {
                    professionService.setProfessionExperience(character, profession, professionService.getProfessionExperience(character, profession) + receivedExperience)
                    val level = professionService.getProfessionLevel(character, profession)
                    val experience = professionService.getProfessionExperience(character, profession)
                    event.whoClicked.sendMessage(plugin.messages["craft-experience", mapOf(
                            "profession" to profession.name.value,
                            "level" to level.toString(),
                            "received_experience" to receivedExperience.toString(),
                            "experience" to (experience - profession.getExperienceNeededForLevel(level)).toString(),
                            "next_level_experience" to (profession.getExperienceNeededForLevel(level + 1) - profession.getExperienceNeededForLevel(level)).toString(),
                            "total_experience" to experience.toString(),
                            "total_next_level_experience" to profession.getExperienceNeededForLevel(level + 1).toString(),
                            "material" to itemType.toString().toLowerCase().replace('_', ' ')
                    )])
                }
            }
        }
    }

    private fun getAmountCrafted(event: CraftItemEvent): Int {
        val currentItem = event.currentItem
        if (currentItem == null || currentItem.type == AIR) {
            return 0
        }
        val cursor = event.cursor
        var amount = event.recipe.result.amount
        if (event.isShiftClick) {
            var max = event.inventory.maxStackSize
            val matrix = event.inventory.matrix
            matrix.asSequence()
                    .filter { it != null && it.type != AIR }
                    .map { it.amount }
                    .filter { it in 1 until max }
                    .forEach { max = it }
            amount *= max
        } else {
            if (cursor != null) {
                if (cursor.type != AIR) {
                    return 0
                }
            }
        }
        var spacesFree = 0
        repeat((event.whoClicked as Player).inventory.storageContents.filter { it == null }.size) {
            spacesFree += event.recipe.result.type.maxStackSize
        }
        (event.whoClicked as Player).inventory.storageContents
                .filter { it != null && it.isSimilar(event.recipe.result) }
                .forEach { spacesFree += it.type.maxStackSize - it.amount }
        if (spacesFree < amount) {
            amount = spacesFree
        }
        return amount
    }

}