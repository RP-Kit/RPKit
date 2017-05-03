package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity
import mkremins.fanciful.FancyMessage
import java.util.*


interface RPKMinecraftProfile: Entity {

    var profile: RPKProfile?
    val minecraftUUID: UUID
    val minecraftUsername: String
    val isOnline: Boolean

    fun sendMessage(message: String)

    fun sendMessage(fancyMessage: FancyMessage)

}