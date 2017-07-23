package com.rpkit.blocklog.bukkit.block

import com.rpkit.core.database.Entity
import org.bukkit.World


interface RPKBlockHistory: Entity {

    val world: World
    val x: Int
    val y: Int
    val z: Int
    val changes: List<RPKBlockChange>
    val inventoryChanges: List<RPKBlockInventoryChange>

}