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

package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: RPKEconomyBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == RIGHT_CLICK_BLOCK) {
            val sign = event.clickedBlock?.state
            if (sign !is Sign) return
            if (sign.getLine(0).equals(ChatColor.GREEN.toString() + "[exchange]", ignoreCase = true)) { // Exchange signs
                val currencyService = Services[RPKCurrencyService::class.java]
                if (currencyService == null) {
                    event.player.sendMessage(plugin.messages["no-currency-service"])
                    return
                }
                val amount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
                val fromCurrency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), "")))
                val toCurrency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(3)))
                if (fromCurrency == null || toCurrency == null) {
                    event.player.sendMessage(plugin.messages["exchange-invalid-format"])
                    return
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                    return
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    event.player.sendMessage(plugin.messages["no-character-service"])
                    return
                }
                val economyService = Services[RPKEconomyService::class.java]
                if (economyService == null) {
                    event.player.sendMessage(plugin.messages["no-economy-service"])
                    return
                }
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
                if (minecraftProfile == null) {
                    event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                    return
                }
                val character = characterService.getActiveCharacter(minecraftProfile)
                if (character == null) {
                    event.player.sendMessage(plugin.messages["no-character"])
                    return
                }
                if (economyService.getBalance(character, fromCurrency) - amount < 0) {
                    event.player.sendMessage(plugin.messages["exchange-invalid-wallet-balance-too-low"])
                    return
                }
                val convertedAmount = fromCurrency.convert(amount, toCurrency)
                if (economyService.getBalance(character, toCurrency) + convertedAmount > 1728) {
                    event.player.sendMessage(plugin.messages["exchange-invalid-wallet-balance-too-high"])
                    return
                }
                economyService.setBalance(character, fromCurrency, economyService.getBalance(character, fromCurrency) - amount)
                economyService.setBalance(character, toCurrency, economyService.getBalance(character, toCurrency) + convertedAmount)
                event.player.sendMessage(plugin.messages["exchange-valid", mapOf(
                    "from_amount" to amount.toString(),
                    "to_amount" to convertedAmount.toString(),
                    "from_currency" to if (amount == 1) fromCurrency.nameSingular else fromCurrency.namePlural,
                    "to_currency" to if (convertedAmount == 1) toCurrency.nameSingular else toCurrency.namePlural
                )])
            } else if (sign.getLine(0).equals(ChatColor.GREEN.toString() + "[dynexchange]", ignoreCase = true)) { // Dynamic exchange signs
                val currencyService = Services[RPKCurrencyService::class.java]
                if (currencyService == null) {
                    event.player.sendMessage(plugin.messages["no-currency-service"])
                    return
                }
                val fromAmount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
                val fromCurrency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), "")))
                val toAmount = sign.getLine(3).split(Regex("\\s+"))[0].toInt()
                val toCurrency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(3).replaceFirst(Regex("\\d+\\s+"), "")))
                if (fromCurrency == null || toCurrency == null) return
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                    return
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    event.player.sendMessage(plugin.messages["no-character-service"])
                    return
                }
                val economyService = Services[RPKEconomyService::class.java]
                if (economyService == null) {
                    event.player.sendMessage(plugin.messages["no-economy-service"])
                    return
                }
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
                val character = characterService.getActiveCharacter(minecraftProfile) ?: return
                if (economyService.getBalance(character, fromCurrency) - fromAmount < 0) return
                if (economyService.getBalance(character, toCurrency) + toAmount > 1728) return
                economyService.setBalance(character, fromCurrency, economyService.getBalance(character, fromCurrency) - fromAmount)
                economyService.setBalance(character, toCurrency, economyService.getBalance(character, toCurrency) + toAmount)
                var rate = fromAmount.toDouble() / toAmount.toDouble()
                val rateChange = plugin.config.getDouble("dynamic-exchanges.rate-change")
                rate -= rateChange
                val newToAmount = Math.round(fromAmount.toDouble() / rate)
                sign.setLine(3, "$newToAmount ${toCurrency.name.value}")
                sign.update()
                event.player.sendMessage(plugin.messages["exchange-valid", mapOf(
                    "from_amount" to fromAmount.toString(),
                    "to_amount" to toAmount.toString(),
                    "from_currency" to if (fromAmount == 1) fromCurrency.nameSingular else fromCurrency.namePlural,
                    "to_currency" to if (toAmount == 1) toCurrency.nameSingular else toCurrency.namePlural
                )])
            }
        } else if (event.action == LEFT_CLICK_BLOCK) {
            val sign = event.clickedBlock?.state
            if (sign !is Sign) return
            if (!sign.getLine(0).equals(ChatColor.GREEN.toString() + "[dynexchange]", ignoreCase = true)) return
            // Dynamic exchange signs
            val currencyService = Services[RPKCurrencyService::class.java]
            if (currencyService == null) {
                event.player.sendMessage(plugin.messages["no-currency-service"])
                return
            }
            val fromAmount = sign.getLine(3).split(Regex("\\s+"))[0].toInt()
            val fromCurrency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(3).replaceFirst(Regex("\\d+\\s+"), "")))
            val toAmount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
            val toCurrency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), "")))
            if (fromCurrency == null || toCurrency == null) return
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                event.player.sendMessage(plugin.messages["no-character-service"])
                return
            }
            val economyService = Services[RPKEconomyService::class.java]
            if (economyService == null) {
                event.player.sendMessage(plugin.messages["no-economy-service"])
                return
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
            val character = characterService.getActiveCharacter(minecraftProfile) ?: return
            if (economyService.getBalance(character, fromCurrency) - fromAmount < 0) return
            if (economyService.getBalance(character, toCurrency) + toAmount > 1728) return
            economyService.setBalance(character, fromCurrency, economyService.getBalance(character, fromCurrency) - fromAmount)
            economyService.setBalance(character, toCurrency, economyService.getBalance(character, toCurrency) + toAmount)
            var rate = fromAmount.toDouble() / toAmount.toDouble()
            val rateChange = plugin.config.getDouble("dynamic-exchanges.rate-change")
            rate += rateChange
            val newToAmount = Math.round(fromAmount.toDouble() / rate)
            sign.setLine(3, "$newToAmount ${toCurrency.name.value}")
            sign.update()
            event.player.sendMessage(plugin.messages["exchange-valid", mapOf(
                "from_amount" to fromAmount.toString(),
                "to_amount" to toAmount.toString(),
                "from_currency" to if (fromAmount == 1) fromCurrency.nameSingular else fromCurrency.namePlural,
                "to_currency" to if (toAmount == 1) toCurrency.nameSingular else toCurrency.namePlural
            )])
        }
    }

}