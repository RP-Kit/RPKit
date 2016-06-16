package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

class ThirstField: CharacterCardField {

    override val name = "thirst"
    override fun get(character: ElysiumCharacter): String {
        return character.thirstLevel.toString()
    }


}
