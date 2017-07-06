package com.rpkit.drink.bukkit.drink

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider
import org.bukkit.inventory.ItemStack


interface RPKDrinkProvider: ServiceProvider {

    val drinks: List<RPKDrink>

    fun getDrunkenness(character: RPKCharacter): Int
    fun setDrunkenness(character: RPKCharacter, drunkenness: Int)
    fun getDrink(name: String): RPKDrink?
    fun getDrink(item: ItemStack): RPKDrink?

}