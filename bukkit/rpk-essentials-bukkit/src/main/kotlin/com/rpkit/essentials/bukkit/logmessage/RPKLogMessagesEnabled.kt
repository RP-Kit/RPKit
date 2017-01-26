package com.rpkit.essentials.bukkit.logmessage

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer


class RPKLogMessagesEnabled(
        override var id: Int = 0,
        val player: RPKPlayer,
        var enabled: Boolean
): Entity