package com.rpkit.blocklog.bukkit.block

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.database.table.RPKBlockChangeTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockInventoryChangeTable
import org.bukkit.World


class RPKBlockHistoryImpl(
        private val plugin: RPKBlockLoggingBukkit,
        override var id: Int = 0,
        override val world: World,
        override val x: Int,
        override val y: Int,
        override val z: Int
): RPKBlockHistory {
    override val changes: List<RPKBlockChange>
        get() = plugin.core.database.getTable(RPKBlockChangeTable::class).get(this)

    override val inventoryChanges: List<RPKBlockInventoryChange>
        get() = plugin.core.database.getTable(RPKBlockInventoryChangeTable::class).get(this)
}