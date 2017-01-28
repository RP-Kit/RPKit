package com.rpkit.locks.bukkit.lock

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer


class RPKPlayerUnclaiming(
        override var id: Int = 0,
        val player: RPKPlayer
) : Entity