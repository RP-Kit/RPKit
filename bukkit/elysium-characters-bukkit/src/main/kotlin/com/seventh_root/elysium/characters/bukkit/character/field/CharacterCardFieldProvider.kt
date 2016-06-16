package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.core.service.ServiceProvider


interface CharacterCardFieldProvider: ServiceProvider {

    val characterCardFields: MutableList<CharacterCardField>

}