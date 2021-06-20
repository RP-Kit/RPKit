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

package com.rpkit.rolling.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern


class RollCommand(private val plugin: RPKRollingBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.rolling.command.roll")) return true
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["roll-usage"])
            return true
        }
        val diePattern = Pattern.compile("[+-]\\d*d\\d+")
        val fullRollString = if (args[0].startsWith("+")) args[0] else "+${args[0]}"
        val dieMatcher = diePattern.matcher(fullRollString)
        var total = 0
        val parsedRollBuilder = StringBuilder()
        while (dieMatcher.find()) {
            val rollString = dieMatcher.group()
            val multiplier = if (rollString.startsWith("-")) -1 else 1
            val rollSections = rollString.split("d")
            val diceAmountString = rollSections[0].drop(1)
            val dieFaces = rollSections[1].toInt()
            val diceAmount = if (diceAmountString.isEmpty()) 1 else diceAmountString.toInt()
            val die = Die(dieFaces)
            for (i in 1..diceAmount) {
                total += multiplier * die.roll()
            }
            parsedRollBuilder.append(rollString)
        }
        val rollStringWithoutDice = fullRollString.replace(Regex("[+-]\\d*d\\d+"), "")
        val literalPattern = Pattern.compile("([+-])(\\d+)(?!d)")
        val literalMatcher = literalPattern.matcher(rollStringWithoutDice)
        while (literalMatcher.find()) {
            val sign = literalMatcher.group(1)
            val amount = literalMatcher.group(2).toInt()
            if (sign == "+") {
                total += amount
            } else if (sign == "-") {
                total -= amount
            }
            parsedRollBuilder.append(sign + amount)
        }
        if (parsedRollBuilder.isEmpty()) {
            sender.sendMessage(plugin.messages["roll-invalid-parse"])
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
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        val radius = plugin.config.getInt("rolls.radius")
        val parsedRoll = if (parsedRollBuilder.startsWith("+"))
            parsedRollBuilder.toString().drop(1)
        else
            parsedRollBuilder.toString()
        sender.world.players
                .filter { player -> player.location.distanceSquared(sender.location) <= radius * radius }
                .forEach {
                    it.sendMessage(plugin.messages["roll", mapOf(
                            Pair("character", character.name),
                            Pair("player", minecraftProfile.name),
                            Pair("roll", total.toString()),
                            Pair("dice", parsedRoll)
                    )])
                }
        return true
    }
}