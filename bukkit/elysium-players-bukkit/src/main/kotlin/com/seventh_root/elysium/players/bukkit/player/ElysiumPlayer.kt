package com.seventh_root.elysium.players.bukkit.player

import com.seventh_root.elysium.core.database.TableRow
import org.bukkit.OfflinePlayer

class ElysiumPlayer(
        override var id: Int = 0,
        var name: String,
        var bukkitPlayer: OfflinePlayer? = null,
        var ircNick: String? = null
): TableRow
