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

package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat
import java.util.*

class SeenCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.seen")) {
            if (args.isNotEmpty()) {
                val player = plugin.server.getOfflinePlayer(args[0])
                if (player.isOnline) {
                    sender.sendMessage(plugin.messages["seen-online", mapOf(
                        "player" to (player.name ?: "")
                    )])
                } else {
                    if (player.lastPlayed != 0L) {
                        val lastPlayed = Date(player.lastPlayed)
                        sender.sendMessage(plugin.messages["seen-date", mapOf(
                            "player" to (player.name ?: ""),
                            "date" to SimpleDateFormat("yyyy-MM-dd").format(lastPlayed),
                            "time" to SimpleDateFormat("HH:mm:ss").format(lastPlayed)
                        )])
                        val millis = System.currentTimeMillis() - player.lastPlayed
                        val second = millis / 1000 % 60
                        val minute = millis / (1000 * 60) % 60
                        val hour = millis / (1000 * 60 * 60) % 24
                        val day = millis / (1000 * 60 * 60 * 24)
                        sender.sendMessage(plugin.messages["seen-diff", mapOf(
                            "days" to day.toString(),
                            "hours" to hour.toString(),
                            "minutes" to minute.toString(),
                            "seconds" to second.toString()
                        )])
                    } else {
                        sender.sendMessage(plugin.messages["seen-never"])
                    }
                }
            } else {
                sender.sendMessage(plugin.messages["seen-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-seen"])
        }
        return true
    }
}
