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

package com.rpkit.characters.bukkit.command.race

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Race list command.
 * Lists all currently available races.
 */
class RaceListCommand(private val plugin: RPKCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.characters.command.race.list")) {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
            sender.sendMessage(plugin.core.messages["race-list-title"])
            for (race in raceProvider.races) {
                sender.sendMessage(plugin.core.messages["race-list-item", mapOf(
                        Pair("race", race.name)
                )])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-race-list"])
        }
        return true
    }

}
