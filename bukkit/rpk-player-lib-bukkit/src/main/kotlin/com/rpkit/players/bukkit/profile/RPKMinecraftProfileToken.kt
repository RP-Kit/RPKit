package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity


interface RPKMinecraftProfileToken: Entity {

    val minecraftProfile: RPKMinecraftProfile
    val token: String

}