/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.rolling.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.rolling.bukkit.roll.Roll
import com.rpkit.rolling.bukkit.roll.RollPartResult
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class RollCommand(private val plugin: RPKRollingBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.rolling.command.roll")) return true
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.rollUsage)
            return true
        }
        val input = Roll.parse(args.joinToString("").replace(Regex("\\s+"), ""))
        val partResultList = input.roll()
        if (partResultList.isEmpty()) {
            sender.sendMessage(plugin.messages.rollInvalidParse)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return true
        }
        val radius = plugin.config.getInt("rolls.radius")
        val parsedRoll = input.toDisplayString()
        val total = partResultList.map(RollPartResult::result).sum()
        val results = partResultList
            .map { rollPartResult ->
                when (rollPartResult.rollPart) {
                    is Roll.Die -> "$AQUA$rollPartResult$WHITE"
                    is Roll.Modifier -> "$YELLOW$rollPartResult$WHITE"
                    else -> rollPartResult.toString()
                }
            }
            .reduce { a, b -> "$a+$b" } + " = $total"
        sender.world.players
                .filter { player -> player.location.distanceSquared(sender.location) <= radius * radius }
                .forEach {
                    it.sendMessage(plugin.messages.roll.withParameters(
                        character = character,
                        player = minecraftProfile,
                        roll = results,
                        dice = parsedRoll
                    ))
                }
        return true
    }
}