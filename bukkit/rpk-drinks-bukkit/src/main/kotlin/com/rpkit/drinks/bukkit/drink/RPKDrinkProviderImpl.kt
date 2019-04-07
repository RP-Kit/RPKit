package com.rpkit.drinks.bukkit.drink

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.drink.bukkit.drink.RPKDrink
import com.rpkit.drink.bukkit.drink.RPKDrinkProvider
import com.rpkit.drinks.bukkit.RPKDrinksBukkit
import com.rpkit.drinks.bukkit.database.table.RPKDrunkennessTable
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe


class RPKDrinkProviderImpl(private val plugin: RPKDrinksBukkit): RPKDrinkProvider {

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
                                            ?: Material.matchMaterial(item, true)
                                            ?: throw IllegalArgumentException("Invalid material $item in recipe for $name")
                            )
                        }
                RPKDrinkImpl(
                        name,
                        drinkItem,
                        recipe,
                        plugin.config.getInt("drinks.$name.drunkenness")
                )
            }
            ?: listOf()

    override fun getDrunkenness(character: RPKCharacter): Int {
        return plugin.core.database.getTable(RPKDrunkennessTable::class).get(character)?.drunkenness?:0
    }

    override fun setDrunkenness(character: RPKCharacter, drunkenness: Int) {
        val drunkennessTable = plugin.core.database.getTable(RPKDrunkennessTable::class)
        var charDrunkenness = drunkennessTable.get(character)
        if (charDrunkenness != null) {
            charDrunkenness.drunkenness = drunkenness
            drunkennessTable.update(charDrunkenness)
        } else {
            charDrunkenness = RPKDrunkenness(character = character, drunkenness = drunkenness)
            drunkennessTable.insert(charDrunkenness)
        }
    }

    override fun getDrink(name: String): RPKDrink? {
        return drinks.firstOrNull { it.name == name }
    }

    override fun getDrink(item: ItemStack): RPKDrink? {
        return drinks.firstOrNull { it.item.isSimilar(item) }
    }

}