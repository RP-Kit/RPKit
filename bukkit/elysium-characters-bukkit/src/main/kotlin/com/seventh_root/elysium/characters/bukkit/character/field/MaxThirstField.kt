package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.api.character.CharacterCardField
import com.seventh_root.elysium.api.character.ElysiumCharacter

class MaxThirstField: CharacterCardField {

    override val name = "max-thirst"
    override fun get(character: ElysiumCharacter): String {
        return 20.toString()
    }


}
