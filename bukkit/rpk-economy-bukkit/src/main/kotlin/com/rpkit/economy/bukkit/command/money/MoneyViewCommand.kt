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
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Money view command.
 * Views the money of a player's active character.
 */
class MoneyViewCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        var bukkitPlayer: Player? = null
        if (sender is Player && sender.hasPermission("rpkit.economy.command.money.view.self")) {
            bukkitPlayer = sender
        }
        if (args.isNotEmpty() && sender.hasPermission("rpkit.economy.command.money.view.other")) {
            bukkitPlayer = plugin.server.getPlayer(args[0])
        }
        if (bukkitPlayer != null) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                val profile = minecraftProfile.profile
                if (profile is RPKProfile) {
                    val character: RPKCharacter?
                    character = if (args.size > 1) {
                        val nameBuilder = StringBuilder()
                        for (i in 1..args.size - 2) {
                            nameBuilder.append(args[i]).append(' ')
                        }
                        nameBuilder.append(args[args.size - 1])
                        val name = nameBuilder.toString()
                        characterProvider.getCharacters(profile)
                                .firstOrNull { character -> character.name == name }
                    } else {
                        characterProvider.getActiveCharacter(minecraftProfile)
                    }
                    if (character != null) {
                        sender.sendMessage(plugin.messages["money-view-valid"])
                        sender.sendMessage(currencyProvider.currencies
                                .map { currency ->
                                    plugin.messages["money-view-valid-list-item", mapOf(
                                            Pair("currency", currency.name),
                                            Pair("balance", economyProvider.getBalance(character, currency).toString())
                                    )]
                                }
                                .toTypedArray()
                        )
                    } else {
                        sender.sendMessage(plugin.messages["no-character"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }

}
