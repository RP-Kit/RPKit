package com.rpkit.players.bukkit.profile

class RPKIRCProfileImpl(
        override var id: Int = 0,
        override val profile: RPKProfile,
        override val nick: String
) : RPKIRCProfile
