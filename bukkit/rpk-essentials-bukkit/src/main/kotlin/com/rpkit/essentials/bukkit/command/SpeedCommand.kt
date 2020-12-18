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
import org.bukkit.entity.Player

class SpeedCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.speed")) {
            var player: Player? = null
            if (sender is Player) {
                player = sender
            }
            var speed = 0f
            if (args.size >= 2 && plugin.server.getPlayer(args[0]) != null) {
                player = plugin.server.getPlayer(args[0])
                try {
                    speed = java.lang.Float.parseFloat(args[1])
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(plugin.messages["speed-invalid-speed-number"])
                }

            } else if (args.isNotEmpty()) {
                try {
                    speed = java.lang.Float.parseFloat(args[0])
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(plugin.messages["speed-invalid-speed-number"])
                }

            } else {
                if (player != null) {
                    player.flySpeed = 0.1f
                    sender.sendMessage(plugin.messages["speed-reset-valid", mapOf(
                        "player" to player.name
                    )])
                    player.sendMessage(plugin.messages["speed-reset-notification", mapOf(
                        "player" to sender.name
                    )])
                }
                return true
            }
            if (player != null) {
                if (speed >= -1 && speed <= 1) {
                    player.flySpeed = speed
                    sender.sendMessage(plugin.messages["speed-set-valid", mapOf(
                        "player" to player.name,
                        "speed" to speed.toString()
                    )])
                    player.sendMessage(plugin.messages["speed-set-notification", mapOf(
                        "player" to sender.name,
                        "speed" to speed.toString()
                    )])
                } else {
                    sender.sendMessage(plugin.messages["speed-invalid-speed-bounds"])
                }
            } else {
                sender.sendMessage(plugin.messages["speed-usage-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-speed"])
        }
        return true
    }

}
