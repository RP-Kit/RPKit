package com.rpkit.warp.bukkit.warp

import com.rpkit.core.database.Entity
import org.bukkit.Location


interface RPKWarp: Entity {

    val name: String
    val location: Location

}