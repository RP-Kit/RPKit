package com.rpkit.essentials.bukkit.drink

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.drink.bukkit.drink.RPKDrinkProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKDrunkennessTable


class RPKDrinkProviderImpl(private val plugin: RPKEssentialsBukkit): RPKDrinkProvider {

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

}