package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.api.character.CharacterCardField
import com.seventh_root.elysium.api.character.ElysiumCharacter


class PlayerField: CharacterCardField {

    override val name = "player"

    override fun get(character: ElysiumCharacter): String {
        return character.player?.name?:"unset"
    }

}