package com.rpkit.drinks.bukkit.drink

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity

class RPKDrunkenness(
        override var id: Int = 0,
        val character: RPKCharacter,
        var drunkenness: Int
): Entity