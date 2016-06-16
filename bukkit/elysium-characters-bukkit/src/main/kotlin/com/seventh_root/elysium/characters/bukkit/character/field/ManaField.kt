package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

class ManaField: CharacterCardField {

    override val name = "mana"
    override fun get(character: ElysiumCharacter): String {
        return character.mana.toString()
    }

}
