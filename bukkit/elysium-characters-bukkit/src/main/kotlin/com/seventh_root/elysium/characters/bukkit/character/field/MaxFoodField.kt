package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

class MaxFoodField: CharacterCardField {

    override val name = "max-food"
    override fun get(character: ElysiumCharacter): String {
        return 20.toString()
    }

}
