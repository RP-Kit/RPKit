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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction.CRAFT
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random


class CraftItemListener(private val plugin: RPKCraftingSkillBukkit): Listener {

    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val bukkitPlayer = event.whoClicked
        if (bukkitPlayer is Player) {
            if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val craftingSkillProvider = plugin.core.serviceManager.getServiceProvider(RPKCraftingSkillProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile == null) {
                event.isCancelled = true
                bukkitPlayer.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            if (character == null) {
                event.isCancelled = true
                bukkitPlayer.sendMessage(plugin.messages["no-character"])
                return
            }
            val itemType = event.recipe.result.type
            val craftingExperience = craftingSkillProvider.getCraftingExperience(character, CRAFT, itemType)
            var amountCrafted = getAmountCrafted(event)
            val craftingSkill = craftingSkillProvider.getCraftingExperience(character, CRAFT, itemType)
            val amount = craftingSkillProvider.getAmountFor(CRAFT, itemType, craftingSkill)
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
            val maxExperience = plugin.config.getConfigurationSection("crafting.$itemType")
                    ?.getKeys(false)
                    ?.map(String::toInt)
                    ?.max()
                    ?: 0
            if (maxExperience != 0 && craftingExperience < maxExperience) {
                val totalExperience = min(craftingExperience + amountCrafted, maxExperience)
                craftingSkillProvider.setCraftingExperience(character, CRAFT, itemType, totalExperience)
                event.whoClicked.sendMessage(plugin.messages["craft-experience", mapOf(
                        Pair("total-experience", totalExperience.toString()),
                        Pair("received-experience", amountCrafted.toString())
                )])
            }
        }
    }

    private fun getAmountCrafted(event: CraftItemEvent): Int {
        val currentItem = event.currentItem
        if (currentItem == null || currentItem.type == Material.AIR) {
            return 0
        }
        val cursor = event.cursor
        var amount = event.recipe.result.amount
        if (event.isShiftClick) {
            var max = event.inventory.maxStackSize
            val matrix = event.inventory.matrix
            matrix.asSequence()
                    .filter { it != null && it.type != Material.AIR }
                    .map { it.amount }
                    .filter { it in 1 until max }
                    .forEach { max = it }
            amount *= max
        } else {
            if (cursor != null) {
                if (cursor.type != Material.AIR) {
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