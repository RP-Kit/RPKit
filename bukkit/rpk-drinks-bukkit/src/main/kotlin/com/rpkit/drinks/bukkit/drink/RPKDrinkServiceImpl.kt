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

package com.rpkit.drinks.bukkit.drink

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.drink.bukkit.drink.RPKDrink
import com.rpkit.drink.bukkit.drink.RPKDrinkName
import com.rpkit.drink.bukkit.drink.RPKDrinkService
import com.rpkit.drink.bukkit.event.drink.RPKBukkitDrunkennessChangeEvent
import com.rpkit.drinks.bukkit.RPKDrinksBukkit
import com.rpkit.drinks.bukkit.database.table.RPKDrunkennessTable
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import java.util.concurrent.CompletableFuture


class RPKDrinkServiceImpl(override val plugin: RPKDrinksBukkit) : RPKDrinkService {

    override val drinks: List<RPKDrink> = plugin.config.getConfigurationSection("drinks")
            ?.getKeys(false)
            ?.mapNotNull { name ->
                val drinkItem = plugin.config.getItemStack("drinks.$name.item") ?: return@mapNotNull null
                val recipe = ShapelessRecipe(NamespacedKey(plugin, name), drinkItem)
                plugin.config.getConfigurationSection("drinks.$name.recipe")
                        ?.getKeys(false)
                        ?.forEach { item ->
                            recipe.addIngredient(plugin.config.getInt("drinks.$name.recipe.$item"),
                                    Material.matchMaterial(item)
                                            ?: throw IllegalArgumentException("Invalid material $item in recipe for $name")
                            )
                        }
                RPKDrinkImpl(
                        RPKDrinkName(name),
                        drinkItem,
                        recipe,
                        plugin.config.getInt("drinks.$name.drunkenness")
                )
            }
            ?: listOf()

    override fun getDrunkenness(character: RPKCharacter): CompletableFuture<Int> {
        return plugin.database.getTable(RPKDrunkennessTable::class.java)[character].thenApply { it?.drunkenness ?: 0 }
    }

    override fun setDrunkenness(character: RPKCharacter, drunkenness: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitDrunkennessChangeEvent(character, getDrunkenness(character).join(), drunkenness, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val drunkennessTable = plugin.database.getTable(RPKDrunkennessTable::class.java)
            var charDrunkenness = drunkennessTable[character].join()
            if (charDrunkenness != null) {
                charDrunkenness.drunkenness = drunkenness
                drunkennessTable.update(charDrunkenness).join()
            } else {
                charDrunkenness = RPKDrunkenness(character = character, drunkenness = drunkenness)
                drunkennessTable.insert(charDrunkenness).join()
            }
        }
    }

    override fun getDrink(name: RPKDrinkName): RPKDrink? {
        return drinks.firstOrNull { it.name.value == name.value }
    }

    override fun getDrink(item: ItemStack): RPKDrink? {
        return drinks.firstOrNull { it.item.isSimilar(item) }
    }

}