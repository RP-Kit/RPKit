package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter


class PlayerField: CharacterCardField {

    override val name = "player"

    override fun get(character: ElysiumCharacter): String {
        return character.player?.name?:"unset"
    }

}