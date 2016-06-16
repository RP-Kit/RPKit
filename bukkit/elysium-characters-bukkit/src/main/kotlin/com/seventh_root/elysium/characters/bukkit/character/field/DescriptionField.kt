package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

class DescriptionField: CharacterCardField {

    override val name = "description"
    override fun get(character: ElysiumCharacter): String {
        return character.description
    }


}
