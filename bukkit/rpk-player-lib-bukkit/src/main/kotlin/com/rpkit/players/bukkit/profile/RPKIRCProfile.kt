package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity


interface RPKIRCProfile: Entity {

    val profile: RPKProfile
    val nick: String

}