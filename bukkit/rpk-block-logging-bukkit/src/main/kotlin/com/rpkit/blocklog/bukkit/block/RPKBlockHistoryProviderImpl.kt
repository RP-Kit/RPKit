package com.rpkit.blocklog.bukkit.block

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.database.table.RPKBlockChangeTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockHistoryTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockInventoryChangeTable
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

    override fun getBlockChange(id: Int): RPKBlockChange? {
        return plugin.core.database.getTable(RPKBlockChangeTable::class).get(id)
    }

    override fun addBlockChange(blockChange: RPKBlockChange) {
        plugin.core.database.getTable(RPKBlockChangeTable::class).insert(blockChange)
    }

    override fun updateBlockChange(blockChange: RPKBlockChange) {
        plugin.core.database.getTable(RPKBlockChangeTable::class).update(blockChange)
    }

    override fun removeBlockChange(blockChange: RPKBlockChange) {
        plugin.core.database.getTable(RPKBlockChangeTable::class).delete(blockChange)
    }

    override fun getBlockInventoryChange(id: Int): RPKBlockInventoryChange? {
        return plugin.core.database.getTable(RPKBlockInventoryChangeTable::class).get(id)
    }

    override fun addBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange) {
        plugin.core.database.getTable(RPKBlockInventoryChangeTable::class).insert(blockInventoryChange)
    }

    override fun updateBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange) {
        plugin.core.database.getTable(RPKBlockInventoryChangeTable::class).update(blockInventoryChange)
    }

    override fun removeBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange) {
        plugin.core.database.getTable(RPKBlockInventoryChangeTable::class).delete(blockInventoryChange)
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

    override fun getBlockDataAtTime(block: Block, time: Long): Byte {
        val history = getBlockHistory(block)
        var data = block.data
        history.changes
                .asReversed()
                .takeWhile { time <= it.time }
                .forEach { data = it.fromData }
        return data
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