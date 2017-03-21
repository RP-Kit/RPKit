package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity
import java.util.*


interface RPKMinecraftProfile: Entity {

    var profile: RPKProfile?
    val minecraftUUID: UUID
    val minecraftUsername: String

}