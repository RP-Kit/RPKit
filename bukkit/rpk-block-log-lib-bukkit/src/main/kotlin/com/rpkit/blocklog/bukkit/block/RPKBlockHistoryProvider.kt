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

import com.rpkit.core.service.Service
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime


interface RPKBlockHistoryService : Service {

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
    fun getBlockTypeAtTime(block: Block, time: LocalDateTime): Material
    fun getBlockInventoryAtTime(block: Block, time: LocalDateTime): Array<ItemStack>

}