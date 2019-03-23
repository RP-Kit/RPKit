package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity


class RPKCharacterClass(
        override var id: Int = 0,
        val character: RPKCharacter,
        var `class`: RPKClass
) : Entity