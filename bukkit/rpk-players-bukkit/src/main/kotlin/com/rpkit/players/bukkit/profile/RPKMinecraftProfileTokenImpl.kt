package com.rpkit.players.bukkit.profile


class RPKMinecraftProfileTokenImpl(
        override var id: Int = 0,
        override val minecraftProfile: RPKMinecraftProfile,
        override val token: String
) : RPKMinecraftProfileToken