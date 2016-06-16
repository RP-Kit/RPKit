package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

class RaceField: CharacterCardField {

    override val name = "race"
    override fun get(character: ElysiumCharacter): String {
        return character.race?.name?:"unset"
    }

}
