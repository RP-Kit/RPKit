package com.seventh_root.elysium.players.bukkit.player

import com.seventh_root.elysium.core.database.TableRow
import org.bukkit.OfflinePlayer

class BukkitPlayer(override var id: Int, var bukkitPlayer: OfflinePlayer) : TableRow, ElysiumPlayer {

    constructor(bukkitPlayer: OfflinePlayer) : this(0, bukkitPlayer) {
    }

    override val name: String
        get() = bukkitPlayer.name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as BukkitPlayer
        return id == that.id
    }

    override fun hashCode(): Int{
        var result = id
        result += 31 * result + bukkitPlayer.hashCode()
        return result
    }

}
