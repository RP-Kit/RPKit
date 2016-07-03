/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.characters.bukkit.command.race

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RaceListCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("elysium.characters.command.race.list")) {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(ElysiumRaceProvider::class)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-title")))
            for (race in raceProvider.races) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-item")
                        .replace("\$race", race.name)))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-race-list")))
        }
        return true
    }

}
