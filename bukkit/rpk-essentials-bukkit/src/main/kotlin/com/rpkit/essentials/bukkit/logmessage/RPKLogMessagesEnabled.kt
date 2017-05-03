package com.rpkit.essentials.bukkit.logmessage

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile


class RPKLogMessagesEnabled(
        override var id: Int = 0,
        val minecraftProfile: RPKMinecraftProfile,
        var enabled: Boolean
): Entity