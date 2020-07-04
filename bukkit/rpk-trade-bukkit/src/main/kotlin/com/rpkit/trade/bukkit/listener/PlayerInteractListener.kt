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

package com.rpkit.trade.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.food.bukkit.expiry.RPKExpiryProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
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
class PlayerInteractListener(private val plugin: RPKTradeBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock
        if (clickedBlock == null) return
        if (clickedBlock.state !is Sign) return
        val sign = clickedBlock.state as Sign
        if (sign.getLine(0) != "$GREEN[trader]") return
        val material = Material.matchMaterial(sign.getLine(1))
                ?: Material.matchMaterial(sign.getLine(1).replace(Regex("\\d+\\s+"), ""))
                ?: Material.matchMaterial(sign.getLine(1), true)
                ?: Material.matchMaterial(sign.getLine(1).replace(Regex("\\d+\\s+"), ""), true)
        if (material == null) {
            event.player.sendMessage(plugin.messages["trader-material-invalid"])
            return
        }
        val amount = if (sign.getLine(1).matches(Regex("\\d+\\s+.*"))) sign.getLine(1).split(Regex("\\s+"))[0].toInt() else 1
        var buyPrice = sign.getLine(2).split(" | ")[0].toInt()
        var sellPrice = sign.getLine(2).split(" | ")[1].toInt()
        var actualPrice = arrayOf(buyPrice, sellPrice).average()
        val currency = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(sign.getLine(3))
        if (currency == null) {
            event.player.sendMessage(plugin.messages["trader-invalid-currency"])
            return
        }
        if (event.action === RIGHT_CLICK_BLOCK) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
            } else {
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                if (character == null) {
                    event.player.sendMessage(plugin.messages["no-character"])
                    return
                }
                if (!event.player.hasPermission("rpkit.trade.sign.trader.buy")) {
                    event.player.sendMessage(plugin.messages["no-permission-trader-buy"])
                    return
                }
                if (economyProvider.getBalance(character, currency) < buyPrice) {
                    event.player.sendMessage(plugin.messages["trader-buy-insufficient-funds"])
                    return
                }
                economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - buyPrice)
                val item = ItemStack(material, amount)
                try {
                    val expiryProvider = plugin.core.serviceManager.getServiceProvider(RPKExpiryProvider::class)
                    expiryProvider.setExpiry(item)
                } catch (ignore: UnregisteredServiceException) {}
                event.player.inventory.addItem(item)
                event.player.sendMessage(plugin.messages["trader-buy", mapOf(
                        Pair("quantity", amount.toString()),
                        Pair("material", material.toString().toLowerCase().replace('_', ' ')),
                        Pair("price", buyPrice.toString() + " " + if (sellPrice == 1) currency.nameSingular else currency.namePlural)
                )])
                actualPrice += plugin.config.getDouble("traders.price-change")
                updatePrices(sign, material, actualPrice)
            }
        } else if (event.action == LEFT_CLICK_BLOCK) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterProvider.getActiveCharacter(minecraftProfile)
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
            if (economyProvider.getBalance(character, currency) + sellPrice > 1728) {
                event.player.sendMessage(plugin.messages["trader-sell-insufficient-wallet-space"])
                return
            }
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + sellPrice)
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
                    Pair("material", material.toString().toLowerCase().replace('_', ' ')),
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