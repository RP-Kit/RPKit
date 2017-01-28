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

import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.shops.bukkit.RPKShopsBukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Player interact listener for shops.
 */
class PlayerInteractListener(val plugin: RPKShopsBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock
            val state = block.state
            if (state is Sign) {
                if (state.getLine(0) == GREEN.toString() + "[shop]") {
                    if (state.getLine(1).startsWith("buy")) {
                        val chestBlock = block.getRelative(DOWN)
                        val chestState = chestBlock.state as? Chest
                        if (chestState != null) {
                            event.player.openInventory(chestState.blockInventory)
                        }
                    } else if (state.getLine(1).startsWith("sell")) {
                        val amount = state.getLine(1).split(Regex("\\s+"))[1].toInt()
                        val material = Material.matchMaterial(state.getLine(1).split(Regex("\\s+"))[2])
                        val price = state.getLine(2).split(Regex("\\s+"))[1].toInt()
                        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                        val currencyBuilder = StringBuilder()
                        for (i in 2..state.getLine(2).split(Regex("\\s+")).size - 1) {
                            currencyBuilder.append(state.getLine(2).split(Regex("\\s+"))[i]).append(' ')
                        }
                        currencyBuilder.deleteCharAt(currencyBuilder.lastIndex)
                        val currency = currencyProvider.getCurrency(currencyBuilder.toString())
                        if (currency != null) {
                            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                            val ownerCharacter = characterProvider.getCharacter(state.getLine(3).toInt())
                            if (ownerCharacter != null) {
                                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                                val customerPlayer = playerProvider.getPlayer(event.player)
                                val customerCharacter = characterProvider.getActiveCharacter(customerPlayer)
                                if (customerCharacter != null) {
                                    val item = ItemStack(material)
                                    val items = ItemStack(material, amount)
                                    if (event.player.inventory.containsAtLeast(item, amount)) {
                                        val chestBlock = block.getRelative(DOWN)
                                        val chestState = chestBlock.state as? Chest
                                        if (chestState != null) {
                                            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                                            val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
                                            if (bankProvider.getBalance(ownerCharacter, currency) >= price) {
                                                event.player.inventory.removeItem(items)
                                                chestState.blockInventory.addItem(items)
                                                bankProvider.setBalance(ownerCharacter, currency, bankProvider.getBalance(ownerCharacter, currency) - price)
                                                economyProvider.setBalance(customerCharacter, currency, economyProvider.getBalance(customerCharacter, currency) + price)
                                            } else {
                                                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-sell-not-enough-money")))
                                            }
                                        } else {
                                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-sell-chest-not-found")))
                                        }
                                    } else {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-sell-not-enough-items")))
                                    }
                                } else {
                                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                                }
                            } else {
                                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-character-invalid")))
                            }
                        } else {
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-currency-invalid")))
                        }
                    }
                }
            }
        }
    }

}