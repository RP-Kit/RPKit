package com.rpkit.drink.bukkit.drink

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe


interface RPKDrink {

    val name: String
    val item: ItemStack
    val recipe: ShapelessRecipe
    val drunkenness: Int

}