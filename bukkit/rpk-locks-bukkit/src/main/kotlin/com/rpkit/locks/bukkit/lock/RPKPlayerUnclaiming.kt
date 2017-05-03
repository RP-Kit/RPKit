package com.rpkit.locks.bukkit.lock

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile


class RPKPlayerUnclaiming(
        override var id: Int = 0,
        val minecraftProfile: RPKMinecraftProfile
) : Entity