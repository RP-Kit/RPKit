package com.rpkit.drinks.bukkit.drink

import com.rpkit.drink.bukkit.drink.RPKDrink
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe


class RPKDrinkImpl(
        override val name: String,
        override val item: ItemStack,
        override val recipe: ShapelessRecipe,
        override val drunkenness: Int
) : RPKDrink