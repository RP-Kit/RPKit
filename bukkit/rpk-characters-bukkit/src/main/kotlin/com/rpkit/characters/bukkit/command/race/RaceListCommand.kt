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

package com.rpkit.characters.bukkit.command.race

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Race list command.
 * Lists all currently available races.
 */
class RaceListCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.characters.command.race.list")) {
            sender.sendMessage(plugin.messages["no-permission-race-list"])
            return true
        }
        val raceService = Services[RPKRaceService::class.java]
        if (raceService == null) {
            sender.sendMessage(plugin.messages["no-race-service"])
            return true
        }
        sender.sendMessage(plugin.messages["race-list-title"])
        for (race in raceService.races) {
            sender.sendMessage(plugin.messages["race-list-item", mapOf(
                "race" to race.name.value
            )])
        }
        return true
    }

}
