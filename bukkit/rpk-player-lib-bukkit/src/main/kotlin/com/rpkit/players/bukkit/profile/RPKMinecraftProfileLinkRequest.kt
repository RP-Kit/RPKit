package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity


interface RPKMinecraftProfileLinkRequest: Entity {

    val profile: RPKProfile
    val minecraftProfile: RPKMinecraftProfile

}