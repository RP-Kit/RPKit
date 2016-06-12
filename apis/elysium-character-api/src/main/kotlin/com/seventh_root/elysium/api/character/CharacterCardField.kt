package com.seventh_root.elysium.api.character


interface CharacterCardField {

    val name: String
    fun get(character: ElysiumCharacter): String

}