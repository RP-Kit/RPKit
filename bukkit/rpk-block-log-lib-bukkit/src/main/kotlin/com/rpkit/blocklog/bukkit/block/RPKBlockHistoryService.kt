/*
 * Copyright 2021 Ren Binden
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
import java.util.concurrent.CompletableFuture


interface RPKBlockHistoryService : Service {

    fun getBlockHistory(id: RPKBlockHistoryId): CompletableFuture<RPKBlockHistory?>
    fun addBlockHistory(blockHistory: RPKBlockHistory): CompletableFuture<Void>
    fun updateBlockHistory(blockHistory: RPKBlockHistory): CompletableFuture<Void>
    fun removeBlockHistory(blockHistory: RPKBlockHistory): CompletableFuture<Void>
    fun getBlockChange(id: RPKBlockChangeId): CompletableFuture<RPKBlockChange?>
    fun addBlockChange(blockChange: RPKBlockChange): CompletableFuture<Void>
    fun updateBlockChange(blockChange: RPKBlockChange): CompletableFuture<Void>
    fun removeBlockChange(blockChange: RPKBlockChange): CompletableFuture<Void>
    fun getBlockInventoryChange(id: RPKBlockInventoryChangeId): CompletableFuture<RPKBlockInventoryChange?>
    fun addBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange): CompletableFuture<Void>
    fun updateBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange): CompletableFuture<Void>
    fun removeBlockInventoryChange(blockInventoryChange: RPKBlockInventoryChange): CompletableFuture<Void>
    fun getBlockHistory(block: Block): CompletableFuture<RPKBlockHistory>
    fun getBlockTypeAtTime(block: Block, time: LocalDateTime): CompletableFuture<Material>
    fun getBlockInventoryAtTime(block: Block, time: LocalDateTime): CompletableFuture<Array<ItemStack>>

}