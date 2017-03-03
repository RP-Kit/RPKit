package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity


class RPKExperienceValue(
        override var id: Int = 0,
        val character: RPKCharacter,
        var value: Int
): Entity