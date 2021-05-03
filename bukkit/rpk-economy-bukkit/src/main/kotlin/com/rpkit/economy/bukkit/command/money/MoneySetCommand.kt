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

package com.rpkit.economy.bukkit.command.money

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Money set command.
 * Sets the amount of money a player's active character has.
 */
class MoneySetCommand(private val plugin: RPKEconomyBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.economy.command.money.set")) {
            sender.sendMessage(plugin.messages.noPermissionMoneySet)
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
        val economyService = Services[RPKEconomyService::class.java]
        if (economyService == null) {
            sender.sendMessage(plugin.messages.noEconomyService)
            return true
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages.noCurrencyService)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.moneySetUsage)
            return true
        }
        val bukkitPlayer = plugin.server.getPlayer(args[0])
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages.moneySetProfileInvalidProfile)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfile)
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.moneySetUsage)
            return true
        }
        characterService.getCharacters(profile).thenAccept { characters ->
            val character = characters.firstOrNull { character -> character.name.startsWith(args[1]) }
            if (character == null) {
                sender.sendMessage(plugin.messages.moneySetCharacterInvalidCharacter)
                return@thenAccept
            }
            if (args.size <= 2) {
                sender.sendMessage(plugin.messages.moneySetUsage)
                return@thenAccept
            }
            val currency = currencyService.getCurrency(RPKCurrencyName(args[2]))
            if (currency == null) {
                sender.sendMessage(plugin.messages.moneySetCurrencyInvalidCurrency)
                return@thenAccept
            }
            if (args.size <= 3) {
                sender.sendMessage(plugin.messages.moneySetUsage)
                return@thenAccept
            }
            try {
                val amount = args[3].toInt()
                if (amount < 0) {
                    sender.sendMessage(plugin.messages.moneySetAmountInvalidAmountNegative)
                    return@thenAccept
                }
                if (amount > 1728) {
                    sender.sendMessage(plugin.messages.moneySetAmountInvalidAmountLimit)
                    return@thenAccept
                }
                economyService.setBalance(character, currency, amount).thenRun {
                    sender.sendMessage(plugin.messages.moneySetAmountValid)
                    sender.sendMessage(plugin.messages.moneySetValid)
                }
            } catch (exception: NumberFormatException) {
                sender.sendMessage(plugin.messages.moneySetAmountInvalidAmountNumber)
            }
        }

        return true
    }

}