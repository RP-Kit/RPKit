package com.rpkit.players.bukkit.profile

import mkremins.fanciful.FancyMessage
import org.bukkit.Bukkit
import java.util.*

class RPKMinecraftProfileImpl(
        override var id: Int = 0,
        override var profile: RPKProfile?,
        override val minecraftUUID: UUID
) : RPKMinecraftProfile {

    override val isOnline: Boolean
        get() = Bukkit.getOfflinePlayer(minecraftUUID).isOnline

    override val minecraftUsername: String
        get() = Bukkit.getOfflinePlayer(minecraftUUID).name

    override fun sendMessage(message: String) {
        Bukkit.getPlayer(minecraftUUID)?.sendMessage(message)
    }

    override fun sendMessage(fancyMessage: FancyMessage) {
        val bukkitPlayer = Bukkit.getPlayer(minecraftUUID)
        if (bukkitPlayer != null) {
            fancyMessage.send(bukkitPlayer)
        }
    }

}
