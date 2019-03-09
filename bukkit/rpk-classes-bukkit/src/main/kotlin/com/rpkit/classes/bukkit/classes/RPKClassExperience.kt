package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity


class RPKClassExperience(
        override var id: Int = 0,
        val character: RPKCharacter,
        val `class`: RPKClass,
        var experience: Int
) : Entity