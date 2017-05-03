package com.rpkit.characters.bukkit.newcharactercooldown

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKProfile

class RPKNewCharacterCooldown(
        override var id: Int = 0,
        val profile: RPKProfile,
        var cooldownTimestamp: Long
): Entity
