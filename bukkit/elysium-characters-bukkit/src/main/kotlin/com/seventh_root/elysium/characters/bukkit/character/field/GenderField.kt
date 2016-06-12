package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.api.character.CharacterCardField
import com.seventh_root.elysium.api.character.ElysiumCharacter


class GenderField: CharacterCardField {

    override val name = "gender"
    override fun get(character: ElysiumCharacter): String {
        return character.gender?.name?:"unset"
    }

}