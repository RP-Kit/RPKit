package com.rpkit.players.bukkit.profile

import org.bukkit.Bukkit
import java.util.*

class RPKMinecraftProfileImpl(
        override var id: Int = 0,
        override var profile: RPKProfile?,
        override val minecraftUUID: UUID
) : RPKMinecraftProfile {

    override val minecraftUsername: String
        get() = Bukkit.getOfflinePlayer(minecraftUUID).name

}
