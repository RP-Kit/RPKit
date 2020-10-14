/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.blocklog.bukkit.block

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.database.table.RPKBlockChangeTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockHistoryTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockInventoryChangeTable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime


class RPKBlockHistoryServiceImpl(override val plugin: RPKBlockLoggingBukkit) : RPKBlockHistoryService {

    override fun getBlockHistory(id: Int): RPKBlockHistory? {
        return plugin.database.getTable(RPKBlockHistoryTable::class)[id]
    }

    override fun addBlockHistory(blockHistory: RPKBlockHistory) {
        plugin.database.getTable(RPKBlockHistoryTable::class).insert(blockHistory)
    }

    override fun updateBlockHistory(blockHistory: RPKBlockHistory) {
        plugin.database.getTable(RPKBlockHistoryTable::class).update(blockHistory)
    }

    override fun removeBlockHistory(blockHistory: RPKBlockHistory) {
        plugin.database.getTable(RPKBlockHistoryTable::class).delete(blockHistory)
    }

    override fun getBlockChange(id: Int): RPKBlockChange? {
        return plugin.database.getTable(RPKBlockChangeTable::class)[id]
    }

    override fun addBlockChange(blockChange: RPKBlockChange) {
        plugin.database.getTable(RPKBlockChangeTable::class).insert(blockChange)
    }

    override fun updateBlockChange(blockChange: RPKBlockChange) {
        plugin.database.getTable(RPKBlockChangeTable::class).update(blockChange)
    }

    override fun removeBlockChange(blockChange: RPKBlockChange) {
        plugin.database.getTable(RPKBlockChangeTable::class).delete(blockChange)
    }

    override fun getBlockInventoryChange(id: Int): RPKBlockInventoryChange? {
        return plugin.database.getTable(RPKBlockInventoryChangeTable::class)[id]
    }

    override fun addBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange) {
        plugin.database.getTable(RPKBlockInventoryChangeTable::class).insert(blockInventoryChange)
    }

    override fun updateBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange) {
        plugin.database.getTable(RPKBlockInventoryChangeTable::class).update(blockInventoryChange)
    }

    override fun removeBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange) {
        plugin.database.getTable(RPKBlockInventoryChangeTable::class).delete(blockInventoryChange)
    }

    override fun getBlockHistory(block: Block): RPKBlockHistory {
        var blockHistory = plugin.database.getTable(RPKBlockHistoryTable::class).get(block)
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

    override fun getBlockTypeAtTime(block: Block, time: LocalDateTime): Material {
        val history = getBlockHistory(block)
        var type = block.type
        history.changes
                .asReversed()
                .takeWhile { time <= it.time }
                .forEach { type = it.from }
        return type
    }

    override fun getBlockInventoryAtTime(block: Block, time: LocalDateTime): Array<ItemStack> {
        val history = getBlockHistory(block)
        var inventoryContents = (block.state as? InventoryHolder)?.inventory?.contents ?: emptyArray<ItemStack>()
        history.inventoryChanges
                .asReversed()
                .takeWhile { time <= it.time }
                .forEach { inventoryContents = it.from }
        return inventoryContents
    }

}