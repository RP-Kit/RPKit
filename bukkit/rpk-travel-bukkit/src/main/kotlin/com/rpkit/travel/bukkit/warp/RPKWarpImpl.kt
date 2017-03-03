package com.rpkit.travel.bukkit.warp

import com.rpkit.warp.bukkit.warp.RPKWarp
import org.bukkit.Location

class RPKWarpImpl(
        override var id: Int = 0,
        override val name: String,
        override val location: Location
): RPKWarp
