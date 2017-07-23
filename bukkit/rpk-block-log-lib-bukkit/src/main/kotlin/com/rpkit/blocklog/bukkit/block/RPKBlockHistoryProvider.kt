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
    fun getBlockHistory(block: Block): RPKBlockHistory
    fun getBlockTypeAtTime(block: Block, time: Long): Material
    fun getBlockInventoryAtTime(block: Block, time: Long): Array<ItemStack>

}