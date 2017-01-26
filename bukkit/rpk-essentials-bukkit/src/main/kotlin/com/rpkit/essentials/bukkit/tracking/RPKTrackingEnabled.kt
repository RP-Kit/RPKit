package com.rpkit.essentials.bukkit.tracking

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity

class RPKTrackingEnabled(
        override var id: Int = 0,
        val character: RPKCharacter,
        var enabled: Boolean
) : Entity