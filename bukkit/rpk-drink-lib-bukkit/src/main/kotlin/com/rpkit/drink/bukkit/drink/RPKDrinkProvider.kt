package com.rpkit.drink.bukkit.drink

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider


interface RPKDrinkProvider: ServiceProvider {

    fun getDrunkenness(character: RPKCharacter): Int
    fun setDrunkenness(character: RPKCharacter, drunkenness: Int)

}