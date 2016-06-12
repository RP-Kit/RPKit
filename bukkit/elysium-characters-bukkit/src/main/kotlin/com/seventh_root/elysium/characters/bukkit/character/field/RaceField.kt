package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.api.character.CharacterCardField
import com.seventh_root.elysium.api.character.ElysiumCharacter

class RaceField: CharacterCardField {

    override val name = "race"
    override fun get(character: ElysiumCharacter): String {
        return character.race?.name?:"unset"
    }

}
