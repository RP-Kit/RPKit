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

package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.core.bukkit.location.toRPKBlockLocation
import com.rpkit.core.service.Services
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent
import java.time.LocalDateTime


class BlockPistonExtendListener(private val plugin: RPKBlockLoggingBukkit) : Listener {

    @EventHandler(priority = MONITOR)
    fun onBlockPistonExtend(event: BlockPistonExtendEvent) {
        val blockHistoryService = Services[RPKBlockHistoryService::class.java] ?: return
        var block = event.block
        var count = 0
        while (block.type != Material.AIR && count < 12) {
            blockHistoryService.getBlockHistory(block.toRPKBlockLocation()).thenAccept { blockHistory ->
                val blockChange = RPKBlockChangeImpl(
                    blockHistory = blockHistory,
                    time = LocalDateTime.now(),
                    profile = null,
                    minecraftProfile = null,
                    character = null,
                    from = block.type,
                    to = block.getRelative(event.direction.oppositeFace).type,
                    reason = "PISTON"
                )
                plugin.server.scheduler.runTask(plugin, Runnable {
                    blockHistoryService.addBlockChange(blockChange)
                })
            }
            block = block.getRelative(event.direction)
            count++
        }
    }

}