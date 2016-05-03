package com.seventh_root.elysium.players.bukkit

import com.seventh_root.elysium.api.player.PlayerProvider
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.players.bukkit.database.table.BukkitPlayerTable
import org.bukkit.OfflinePlayer

class BukkitPlayerProvider(private val plugin: ElysiumPlayersBukkit) : PlayerProvider<BukkitPlayer> {

    override fun getPlayer(id: Int): BukkitPlayer? {
        return plugin.core.database.getTable(BukkitPlayer::class.java)!![id]
    }

    fun getPlayer(bukkitPlayer: OfflinePlayer): BukkitPlayer {
        val table = plugin.core.database.getTable(BukkitPlayer::class.java)
        var player = (table as BukkitPlayerTable)[bukkitPlayer]
        if (player == null) {
            player = BukkitPlayer(bukkitPlayer)
            addPlayer(player)
        }
        return player
    }

    override fun addPlayer(player: BukkitPlayer) {
        val bukkitPlayerTable: Table<BukkitPlayer> = plugin.core.database.getTable(BukkitPlayer::class.java) as Table<BukkitPlayer>
        bukkitPlayerTable.insert(player)
    }

    override fun removePlayer(player: BukkitPlayer) {
        plugin.core.database.getTable(BukkitPlayer::class.java)!!.delete(player)
    }

}
