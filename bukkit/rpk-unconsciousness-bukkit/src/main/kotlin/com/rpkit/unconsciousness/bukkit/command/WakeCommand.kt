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

package com.rpkit.unconsciousness.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class WakeCommand(private val plugin: RPKUnconsciousnessBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.unconsciousness.command.wake")) {
            sender.sendMessage(plugin.messages["no-permission-wake"])
            return true
        }
        val target = if (args.isNotEmpty()) plugin.server.getPlayer(args[0]) ?: sender as? Player else sender as? Player
        if (target == null) {
            sender.sendMessage(plugin.messages["wake-no-target"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val unconsciousnessService = Services[RPKUnconsciousnessService::class.java]
        if (unconsciousnessService == null) {
            sender.sendMessage(plugin.messages["no-unconsciousness-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(target)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-other", mapOf(
                    "player" to target.name
            )])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character-other", mapOf(
                    "player" to minecraftProfile.name
            )])
            return true
        }
        if (!unconsciousnessService.isUnconscious(character).join()) {
            sender.sendMessage(plugin.messages["wake-already-awake", mapOf(
                    "character" to character.name
            )])
            return true
        }
        unconsciousnessService.setUnconscious(character, false).thenRun {
            sender.sendMessage(plugin.messages["wake-success", mapOf(
                            "character" to character.name
                    )])
        }

        return true
    }
}
