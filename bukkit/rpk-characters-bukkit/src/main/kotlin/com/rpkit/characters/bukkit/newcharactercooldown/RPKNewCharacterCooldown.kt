package com.rpkit.characters.bukkit.newcharactercooldown

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer

class RPKNewCharacterCooldown(
        override var id: Int = 0,
        val player: RPKPlayer,
        var cooldownTimestamp: Long
): Entity
