package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.api.character.CharacterCardField
import com.seventh_root.elysium.api.character.ElysiumCharacter


class MaxManaField: CharacterCardField {

    override val name = "max-mana"
    override fun get(character: ElysiumCharacter): String {
        return character.maxMana.toString()
    }

}