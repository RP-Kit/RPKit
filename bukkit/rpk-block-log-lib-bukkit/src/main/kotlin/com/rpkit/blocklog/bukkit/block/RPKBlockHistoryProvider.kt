package com.rpkit.blocklog.bukkit.block

import com.rpkit.core.service.ServiceProvider
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack


interface RPKBlockHistoryProvider: ServiceProvider {

    fun getBlockHistory(id: Int): RPKBlockHistory?
    fun addBlockHistory(blockHistory: RPKBlockHistory)
    fun updateBlockHistory(blockHistory: RPKBlockHistory)
    fun removeBlockHistory(blockHistory: RPKBlockHistory)
    fun getBlockChange(id: Int): RPKBlockChange?
    fun addBlockChange(blockChange: RPKBlockChange)
    fun updateBlockChange(blockChange: RPKBlockChange)
    fun removeBlockChange(blockChange: RPKBlockChange)
    fun getBlockInventoryChange(id: Int): RPKBlockInventoryChange?
    fun addBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange)
    fun updateBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange)
    fun removeBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange)
    fun getBlockHistory(block: Block): RPKBlockHistory
    fun getBlockTypeAtTime(block: Block, time: Long): Material
    fun getBlockDataAtTime(block: Block, time: Long): Byte
    fun getBlockInventoryAtTime(block: Block, time: Long): Array<ItemStack>

}