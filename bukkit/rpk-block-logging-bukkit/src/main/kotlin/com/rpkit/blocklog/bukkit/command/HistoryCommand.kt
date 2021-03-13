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

package com.rpkit.blocklog.bukkit.command

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChange
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class HistoryCommand(private val plugin: RPKBlockLoggingBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.blocklogging.command.history")) {
            sender.sendMessage(plugin.messages["no-permission-history"])
            return true
        }
        val targetBlock = sender.getTargetBlock(null, 8)
        val blockHistoryService = Services[RPKBlockHistoryService::class.java]
        if (blockHistoryService == null) {
            sender.sendMessage(plugin.messages["no-block-history-service"])
            return true
        }
        blockHistoryService.getBlockHistory(targetBlock).thenAccept { blockHistory ->
            blockHistory.changes.thenAccept getChanges@{ changes ->
                if (changes.isEmpty()) {
                    sender.sendMessage(plugin.messages["history-no-changes"])
                    return@getChanges
                }
                for (change in changes.sortedBy(RPKBlockChange::time).take(100)) {
                    sender.sendMessage(
                        plugin.messages["history-change", mapOf(
                            "time" to DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")
                                .format(change.time.atZone(ZoneId.systemDefault())),
                            "profile" to (change.profile?.name?.value ?: "None"),
                            "minecraft_profile" to (change.minecraftProfile?.name ?: "None"),
                            "character" to (change.character?.name ?: "None"),
                            "from" to change.from.toString().toLowerCase().replace('_', ' '),
                            "to" to change.to.toString().toLowerCase().replace('_', ' '),
                            "reason" to change.reason
                        )]
                    )
                }
            }
        }
        return true
    }

}