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

class FlyCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.fly")) {
            var player: Player? = null
            if (sender is Player) {
                player = sender
            }
            if (args.isNotEmpty()) {
                if (plugin.server.getPlayer(args[0]) != null) {
                    player = plugin.server.getPlayer(args[0])
                }
            }
            if (player != null) {
                player.allowFlight = !player.allowFlight
                if (player.allowFlight) {
                    player.sendMessage(plugin.messages["fly-enable-notification"])
                    sender.sendMessage(plugin.messages["fly-enable-valid", mapOf(
                        "player" to player.name
                    )])
                } else {
                    player.sendMessage(plugin.messages["fly-disable-notification"])
                    sender.sendMessage(plugin.messages["fly-disable-valid", mapOf(
                        "player" to player.name
                    )])
                }
            } else {
                sender.sendMessage(plugin.messages["fly-usage-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-fly"])
        }
        return true
    }

}
