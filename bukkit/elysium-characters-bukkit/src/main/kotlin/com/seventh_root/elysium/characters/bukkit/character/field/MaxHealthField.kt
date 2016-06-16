package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter


class MaxHealthField: CharacterCardField {

    override val name = "max-health"
    override fun get(character: ElysiumCharacter): String {
        return character.maxHealth.toString()
    }

}