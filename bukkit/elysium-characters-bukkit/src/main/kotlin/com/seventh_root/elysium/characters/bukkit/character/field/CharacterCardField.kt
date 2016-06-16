package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter


interface CharacterCardField {

    val name: String
    fun get(character: ElysiumCharacter): String

}