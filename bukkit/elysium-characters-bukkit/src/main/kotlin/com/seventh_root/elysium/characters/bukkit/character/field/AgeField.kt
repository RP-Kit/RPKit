package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

class AgeField: CharacterCardField {

    override val name = "age"
    override fun get(character: ElysiumCharacter): String {
        return character.age.toString()
    }

}
