/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.shops.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.shopcount.RPKShopCountProvider
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.Material.CHEST
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

/**
 * Sign change listener for shops.
 */
class SignChangeListener(private val plugin: RPKShopsBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0) == "[shop]") {
            if (event.player.hasPermission("rpkit.shops.sign.shop")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                val shopCountProvider = plugin.core.serviceManager.getServiceProvider(RPKShopCountProvider::class)
                val player = playerProvider.getPlayer(event.player)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    val shopCount = shopCountProvider.getShopCount(character)
                    if (shopCount < plugin.config.getInt("shops.limit") || event.player.hasPermission("rpkit.shops.sign.shop.nolimit")) {
                        if (!(event.getLine(1).matches(Regex("buy\\s+\\d+"))
                                || (event.getLine(1).matches(Regex("sell\\s+\\d+\\s+.+"))
                                && Material.matchMaterial(event.getLine(1).replace(Regex("sell\\s+\\d+\\s+"), "")) != null))) {
                            event.block.breakNaturally()
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-line-1-invalid")))
                            return
                        }
                        if (!(event.getLine(2).matches(Regex("for\\s+\\d+\\s+.+")) && currencyProvider.getCurrency(event.getLine(2).replace(Regex("for\\s+\\d+\\s+"), "")) != null)) {
                            event.block.breakNaturally()
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-line-2-invalid")))
                            return
                        }
                        event.setLine(0, GREEN.toString() + "[shop]")
                        event.setLine(3, character.id.toString())
                        event.block.getRelative(DOWN).type = CHEST
                        val chest = event.block.getRelative(DOWN).state
                        if (chest is Chest) {
                            val chestData = chest.data
                            if (chestData is org.bukkit.material.Chest) {
                                val sign = event.block.state
                                if (sign is Sign) {
                                    val signData = sign.data
                                    if (signData is org.bukkit.material.Sign) {
                                        chestData.setFacingDirection(signData.facing)
                                        chest.update()
                                    }
                                }
                            }
                        }
                        shopCountProvider.setShopCount(character, shopCountProvider.getShopCount(character) + 1)
                    } else {
                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-shop-limit")))
                        event.block.breakNaturally()
                    }
                } else {
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-shop")))
                event.block.breakNaturally()
            }
        }
    }

}