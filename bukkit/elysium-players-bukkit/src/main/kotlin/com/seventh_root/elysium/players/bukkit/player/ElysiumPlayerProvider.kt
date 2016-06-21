package com.seventh_root.elysium.players.bukkit.player

import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.database.table.ElysiumPlayerTable
import org.bukkit.OfflinePlayer
import org.pircbotx.User

class ElysiumPlayerProvider(private val plugin: ElysiumPlayersBukkit): ServiceProvider {

    fun getPlayer(id: Int): ElysiumPlayer? {
        return plugin.core.database.getTable(ElysiumPlayer::class.java)!![id]
    }

    fun getPlayer(bukkitPlayer: OfflinePlayer): ElysiumPlayer {
        val table = plugin.core.database.getTable(ElysiumPlayer::class.java)
        var player = (table as ElysiumPlayerTable).get(bukkitPlayer)
        if (player == null) {
            player = ElysiumPlayer(
                    name = bukkitPlayer.name,
                    bukkitPlayer = bukkitPlayer
            )
            addPlayer(player)
        }
        return player
    }

    fun getPlayer(ircUser: User): ElysiumPlayer {
        val table = plugin.core.database.getTable(ElysiumPlayer::class.java)
        var player = (table as ElysiumPlayerTable).get(ircUser)
        if (player == null) {
            player = ElysiumPlayer(
                    name = ircUser.nick,
                    ircNick = ircUser.nick
            )
            addPlayer(player)
        }
        return player
    }

    fun addPlayer(player: ElysiumPlayer) {
        val bukkitPlayerTable: Table<ElysiumPlayer> = plugin.core.database.getTable(ElysiumPlayer::class.java) as Table<ElysiumPlayer>
        bukkitPlayerTable.insert(player)
    }

    fun removePlayer(player: ElysiumPlayer) {
        plugin.core.database.getTable(ElysiumPlayer::class.java)!!.delete(player)
    }

}
