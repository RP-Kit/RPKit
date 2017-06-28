package com.rpkit.essentials.bukkit.locationhistory

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.Location


class RPKPreviousLocation(
        override var id: Int = 0,
        val minecraftProfile: RPKMinecraftProfile,
        var location: Location
) : Entity