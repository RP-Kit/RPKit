package com.seventh_root.elysium.chat.bukkit.chatgroup

import com.seventh_root.elysium.core.database.TableRow
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import java.util.*

class ElysiumChatGroup(
        override var id: Int = 0,
        var name: String,
        players: List<ElysiumPlayer>,
        invited: List<ElysiumPlayer>
): TableRow {

    private val players0 = mutableListOf<ElysiumPlayer>()
    val players: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(players0)
    private val invited0 = mutableListOf<ElysiumPlayer>()
    val invited: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(invited0)

    init {
        players0.addAll(players)
        invited0.addAll(invited)
    }

    fun addPlayer(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun removePlayer(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun invite(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun uninvite(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun sendMessage(sender: ElysiumPlayer, message: String) {
        throw UnsupportedOperationException()
    }

}
