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
import org.bukkit.entity.Player

/**
 * Money subtract command.
 * Takes money from a player's active character.
 */
class MoneySubtractCommand(val plugin: RPKEconomyBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.economy.command.money.subtract")) {
            sender.sendMessage(plugin.messages.noPermissionMoneySubtract)
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
            sender.sendMessage(plugin.messages.moneySubtractUsage)
            return true
        }
        val bukkitPlayer = plugin.server.getPlayer(args[0])
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages.moneySubtractProfileInvalidProfile)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noProfile)
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfile)
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.moneySubtractUsage)
            return true
        }
        characterService.getCharacters(profile).thenAccept { characters ->
            val character = characters.firstOrNull { character -> character.name.startsWith(args[1]) }
            if (character == null) {
                sender.sendMessage(plugin.messages.moneySubtractCharacterInvalidCharacter)
                return@thenAccept
            }
            if (args.size <= 2) {
                sender.sendMessage(plugin.messages.moneySubtractUsage)
                return@thenAccept
            }
            val currency = currencyService.getCurrency(RPKCurrencyName(args[2]))
            if (currency == null) {
                sender.sendMessage(plugin.messages.moneySubtractCurrencyInvalidCurrency)
                return@thenAccept
            }
            if (args.size <= 3) {
                sender.sendMessage(plugin.messages.moneySubtractUsage)
                return@thenAccept
            }
            try {
                val amount = args[3].toInt()
                if (amount < 0) {
                    sender.sendMessage(plugin.messages.moneySubtractAmountInvalidAmountNegative)
                    return@thenAccept
                }
                val walletBalance = economyService.getPreloadedBalance(character, currency)
                if (walletBalance == null) {
                    sender.sendMessage(plugin.messages.noPreloadedBalanceOther.withParameters(character = character))
                    return@thenAccept
                }
                economyService.setBalance(character, currency, walletBalance - amount)
                sender.sendMessage(plugin.messages.moneySubtractAmountValid)
                sender.sendMessage(plugin.messages.moneySubtractValid)
            } catch (exception: NumberFormatException) {
                sender.sendMessage(plugin.messages.moneySubtractAmountInvalidAmountNumber)
            }
        }
        return true
    }

}