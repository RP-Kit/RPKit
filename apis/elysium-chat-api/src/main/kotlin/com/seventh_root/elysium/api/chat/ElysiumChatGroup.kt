package com.seventh_root.elysium.api.chat

import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.core.database.TableRow

interface ElysiumChatGroup : TableRow {

    var name: String
    val players: Collection<ElysiumPlayer>
    fun addPlayer(player: ElysiumPlayer)
    fun removePlayer(player: ElysiumPlayer)
    val invited: Collection<ElysiumPlayer>
    fun invite(player: ElysiumPlayer)
    fun uninvite(player: ElysiumPlayer)
    fun sendMessage(sender: ElysiumPlayer, message: String)

}
