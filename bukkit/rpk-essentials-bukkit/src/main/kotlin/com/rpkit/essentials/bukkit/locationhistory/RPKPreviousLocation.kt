package com.rpkit.essentials.bukkit.locationhistory

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer
import org.bukkit.Location


class RPKPreviousLocation(
        override var id: Int = 0,
        val player: RPKPlayer,
        var location: Location
) : Entity