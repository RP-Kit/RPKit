package com.rpkit.blocklog.bukkit.block

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.database.table.RPKBlockHistoryTable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack


class RPKBlockHistoryProviderImpl(private val plugin: RPKBlockLoggingBukkit): RPKBlockHistoryProvider {

    override fun getBlockHistory(id: Int): RPKBlockHistory? {
        return plugin.core.database.getTable(RPKBlockHistoryTable::class).get(id)
    }

    override fun addBlockHistory(blockHistory: RPKBlockHistory) {
        plugin.core.database.getTable(RPKBlockHistoryTable::class).insert(blockHistory)
    }

    override fun updateBlockHistory(blockHistory: RPKBlockHistory) {
        plugin.core.database.getTable(RPKBlockHistoryTable::class).update(blockHistory)
    }

    override fun removeBlockHistory(blockHistory: RPKBlockHistory) {
        plugin.core.database.getTable(RPKBlockHistoryTable::class).delete(blockHistory)
    }


    override fun getBlockHistory(block: Block): RPKBlockHistory {
        var blockHistory = plugin.core.database.getTable(RPKBlockHistoryTable::class).get(block)
        if (blockHistory == null) {
            blockHistory = RPKBlockHistoryImpl(
                    plugin,
                    world = block.world,
                    x = block.x,
                    y = block.y,
                    z = block.z
            )
            addBlockHistory(blockHistory)
        }
        return blockHistory
    }

    override fun getBlockTypeAtTime(block: Block, time: Long): Material {
        val history = getBlockHistory(block)
        var type = block.type
        history.changes
                .asReversed()
                .takeWhile { time <= it.time }
                .forEach { type = it.from }
        return type
    }

    override fun getBlockInventoryAtTime(block: Block, time: Long): Array<ItemStack> {
        val history = getBlockHistory(block)
        var inventoryContents = (block.state as? InventoryHolder)?.inventory?.contents?:emptyArray<ItemStack>()
        history.inventoryChanges
                .asReversed()
                .takeWhile { time <= it.time }
                .forEach { inventoryContents = it.from }
        return inventoryContents
    }

}