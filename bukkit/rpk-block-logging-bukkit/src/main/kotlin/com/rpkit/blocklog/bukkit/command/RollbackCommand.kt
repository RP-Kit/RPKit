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

package com.rpkit.blocklog.bukkit.command

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.blocklog.bukkit.event.blocklog.RPKBukkitBlockRollbackEvent
import com.rpkit.core.bukkit.location.toRPKBlockLocation
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES


class RollbackCommand(private val plugin: RPKBlockLoggingBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.blocklogging.command.rollback")) {
            sender.sendMessage(plugin.messages["no-permission-rollback"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["rollback-usage"])
            return true
        }
        val blockHistoryService = Services[RPKBlockHistoryService::class.java]
        if (blockHistoryService == null) {
            sender.sendMessage(plugin.messages["no-block-history-service"])
            return true
        }
        val radius = args[0].toIntOrNull()
        if (radius == null || radius <= 0) {
            sender.sendMessage(plugin.messages["rollback-invalid-radius"])
            return true
        }
        val minutes = args[1].toIntOrNull()
        if (minutes == null || minutes <= 0) {
            sender.sendMessage(plugin.messages["rollback-invalid-time"])
            return true
        }
        val time = LocalDateTime.now().minus(Duration.of(minutes.toLong(), MINUTES))
        for (x in (sender.location.blockX - radius)..(sender.location.blockX + radius)) {
            for (y in (sender.location.blockY - radius)..(sender.location.blockY + radius)) {
                for (z in (sender.location.blockZ - radius)..(sender.location.blockZ + radius)) {
                    val block = sender.world.getBlockAt(x, y, z)
                    val event = RPKBukkitBlockRollbackEvent(block, time, false)
                    plugin.server.pluginManager.callEvent(event)
                    if (event.isCancelled) continue
                    blockHistoryService.getBlockTypeAtTime(event.block.toRPKBlockLocation(), event.time).thenAccept { type ->
                        blockHistoryService.getBlockInventoryAtTime(event.block.toRPKBlockLocation(), event.time).thenAccept { inventoryContents ->
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                block.type = type
                                val state = block.state
                                if (state is InventoryHolder) {
                                    state.inventory.contents = inventoryContents
                                    state.update()
                                }
                            })
                        }
                    }
                }
            }
        }
        sender.sendMessage(plugin.messages["rollback-valid"])
        return true
    }

}