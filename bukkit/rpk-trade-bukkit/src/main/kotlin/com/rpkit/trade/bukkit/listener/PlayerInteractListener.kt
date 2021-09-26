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

package com.rpkit.trade.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.food.bukkit.expiry.RPKExpiryService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.trade.bukkit.RPKTradeBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Player interact listener for trader signs.
 */
class PlayerInteractListener(private val plugin: RPKTradeBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.state !is Sign) return
        val sign = clickedBlock.state as Sign
        if (sign.getLine(0) != "$GREEN[trader]") return
        val material = Material.matchMaterial(sign.getLine(1))
                ?: Material.matchMaterial(sign.getLine(1).replace(Regex("\\d+\\s+"), ""))
        if (material == null) {
            event.player.sendMessage(plugin.messages["trader-material-invalid"])
            return
        }
        val amount = if (sign.getLine(1).matches(Regex("\\d+\\s+.*"))) sign.getLine(1).split(Regex("\\s+"))[0].toInt() else 1
        val buyPrice = sign.getLine(2).split(" | ")[0].toInt()
        val sellPrice = sign.getLine(2).split(" | ")[1].toInt()
        var actualPrice = arrayOf(buyPrice, sellPrice).average()
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            event.player.sendMessage(plugin.messages["no-currency-service"])
            return
        }
        val currency = currencyService.getCurrency(RPKCurrencyName(sign.getLine(3)))
        if (currency == null) {
            event.player.sendMessage(plugin.messages["trader-invalid-currency"])
            return
        }
        if (event.action === RIGHT_CLICK_BLOCK) {
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
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
            } else {
                val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
                if (character == null) {
                    event.player.sendMessage(plugin.messages["no-character"])
                    return
                }
                if (!event.player.hasPermission("rpkit.trade.sign.trader.buy")) {
                    event.player.sendMessage(plugin.messages["no-permission-trader-buy"])
                    return
                }
                val walletBalance = economyService.getPreloadedBalance(character, currency)
                if (walletBalance == null) {
                    event.player.sendMessage(plugin.messages.noPreloadedBalance)
                    return
                }
                if (walletBalance < buyPrice) {
                    event.player.sendMessage(plugin.messages["trader-buy-insufficient-funds"])
                    return
                }
                economyService.setBalance(character, currency, walletBalance - buyPrice)
                val item = ItemStack(material, amount)
                Services[RPKExpiryService::class.java]?.setExpiry(item)
                event.player.inventory.addItem(item)
                event.player.sendMessage(plugin.messages["trader-buy", mapOf(
                        Pair("quantity", amount.toString()),
                        Pair("material", material.toString().lowercase().replace('_', ' ')),
                        Pair("price", buyPrice.toString() + " " + if (sellPrice == 1) currency.nameSingular else currency.namePlural)
                )])
                actualPrice += plugin.config.getDouble("traders.price-change")
                updatePrices(sign, material, actualPrice)
            }
        } else if (event.action == LEFT_CLICK_BLOCK) {
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
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
            if (character == null) {
                event.player.sendMessage(plugin.messages["no-character"])
                return
            }
            if (!event.player.hasPermission("rpkit.trade.sign.trader.sell")) {
                event.player.sendMessage(plugin.messages["no-permission-trader-sell"])
                return
            }
            if (!event.player.inventory.contains(material, amount)) {
                event.player.sendMessage(plugin.messages["trader-sell-insufficient-items"])
                return
            }
            val walletBalance = economyService.getPreloadedBalance(character, currency)
            if (walletBalance == null) {
                event.player.sendMessage(plugin.messages.noPreloadedBalance)
                return
            }
            if (walletBalance + sellPrice > 1728) {
                event.player.sendMessage(plugin.messages["trader-sell-insufficient-wallet-space"])
                return
            }
            economyService.setBalance(character, currency, walletBalance + sellPrice)
            var amountRemaining = amount
            val contents = event.player.inventory.contents
            contents
                    .asSequence()
                    .filter { it != null && it.type == material }
                    .forEach {
                        if (it.amount > amountRemaining) {
                            it.amount = it.amount - amountRemaining
                            amountRemaining = 0
                        } else if (it.amount <= amountRemaining) {
                            if (amountRemaining > 0) {
                                amountRemaining -= it.amount
                                it.type = Material.AIR
                            }
                        }
                    }
            event.player.inventory.contents = contents
            event.player.sendMessage(plugin.messages["trader-sell", mapOf(
                    Pair("quantity", amount.toString()),
                    Pair("material", material.toString().lowercase().replace('_', ' ')),
                    Pair("price", sellPrice.toString() + " " + if (sellPrice == 1) currency.nameSingular else currency.namePlural)
            )])
            actualPrice -= plugin.config.getDouble("traders.price-change")
            updatePrices(sign, material, actualPrice)
        }
    }

    private fun updatePrices(sign: Sign, material: Material?, actualPrice: Double) {
        val maximumPrice = plugin.config.getDouble("traders.maximum-price.$material",
                plugin.config.getDouble("traders.maximum-price.default"))
        val minimumPrice = plugin.config.getDouble("traders.minimum-price.$material",
                plugin.config.getDouble("traders.minimum-price.default"))
        val clampedActualPrice = actualPrice.coerceAtMost(maximumPrice).coerceAtLeast(minimumPrice)
        val buyPrice = (clampedActualPrice + ((plugin.config.getDouble("traders.trade-fee-percentage") / 100.0) * clampedActualPrice)).toInt()
        val sellPrice = (clampedActualPrice - ((plugin.config.getDouble("traders.trade-fee-percentage") / 100.0) * clampedActualPrice)).toInt()
        sign.setLine(2, "$buyPrice | $sellPrice")
        sign.update()
    }

}