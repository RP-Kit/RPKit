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

package com.rpkit.economy.bukkit.command.money

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Money view command.
 * Views the money of a player's active character.
 */
class MoneyViewCommand(private val plugin: RPKEconomyBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
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
        val economyService = Services[RPKEconomyService::class.java]
        if (economyService == null) {
            sender.sendMessage(plugin.messages["no-economy-service"])
            return true
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages["no-currency-service"])
            return true
        }
        var bukkitPlayer: Player? = null
        if (sender is Player && sender.hasPermission("rpkit.economy.command.money.view.self")) {
            bukkitPlayer = sender
        }
        if (args.isNotEmpty() && sender.hasPermission("rpkit.economy.command.money.view.other")) {
            bukkitPlayer = plugin.server.getPlayer(args[0])
        }
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val character: RPKCharacter?
        character = if (args.size > 1) {
            val nameBuilder = StringBuilder()
            for (i in 1..args.size - 2) {
                nameBuilder.append(args[i]).append(' ')
            }
            nameBuilder.append(args[args.size - 1])
            val name = nameBuilder.toString()
            characterService.getCharacters(profile)
                    .firstOrNull { profileCharacter -> profileCharacter.name == name }
        } else {
            characterService.getActiveCharacter(minecraftProfile)
        }
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        sender.sendMessage(plugin.messages["money-view-valid"])
        sender.sendMessage(currencyService.currencies
                .map { currency ->
                    plugin.messages["money-view-valid-list-item", mapOf(
                            Pair("currency", currency.name),
                            Pair("balance", economyService.getBalance(character, currency).toString())
                    )]
                }
                .toTypedArray()
        )
        return true
    }

}
