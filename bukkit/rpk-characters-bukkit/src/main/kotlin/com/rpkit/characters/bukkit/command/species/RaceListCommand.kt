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

package com.rpkit.characters.bukkit.command.species

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.species.RPKSpeciesService
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Species list command.
 * Lists all currently available races.
 */
class RaceListCommand(private val plugin: RPKCharactersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.characters.command.species.list")) {
            sender.sendMessage(plugin.messages.noPermissionSpeciesList)
            return true
        }
        val speciesService = Services[RPKSpeciesService::class.java]
        if (speciesService == null) {
            sender.sendMessage(plugin.messages.noSpeciesService)
            return true
        }
        sender.sendMessage(plugin.messages.speciesListTitle)
        for (species in speciesService.species) {
            sender.sendMessage(plugin.messages.speciesListItem.withParameters(species = species))
        }
        return true
    }

}
