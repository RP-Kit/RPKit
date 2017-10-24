package com.rpkit.players.bukkit.profile


class RPKMinecraftProfileLinkRequestImpl(
        override var id: Int = 0,
        override val profile: RPKProfile,
        override val minecraftProfile: RPKMinecraftProfile
) : RPKMinecraftProfileLinkRequest