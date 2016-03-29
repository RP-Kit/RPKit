package com.seventh_root.elysium.api.player

import com.seventh_root.elysium.core.service.ServiceProvider

interface PlayerProvider<T : ElysiumPlayer> : ServiceProvider {

    fun getPlayer(id: Int): T?
    fun addPlayer(player: T)
    fun removePlayer(player: T)

}
