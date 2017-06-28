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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.shops.bukkit.RPKShopsBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Inventory click listener for shops.
 */
class InventoryClickListener(val plugin: RPKShopsBukkit): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val chest = event.inventory.holder
        if (chest is Chest) {
            val sign = chest.block.getRelative(UP).state
            if (sign is Sign) {
                if (sign.getLine(0) == GREEN.toString() + "[shop]") {
                    val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                    val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                    val sellerCharacter = if (sign.getLine(3).equals("admin", ignoreCase = true)) null else characterProvider.getCharacter(sign.getLine(3).toInt()) ?: return
                    val buyerBukkitPlayer = event.whoClicked as? Player ?: return
                    val buyerMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(buyerBukkitPlayer)
                    if (buyerMinecraftProfile != null) {
                        val buyerCharacter = characterProvider.getActiveCharacter(buyerMinecraftProfile)
                        if (buyerCharacter == null) {
                            buyerBukkitPlayer.sendMessage(plugin.messages["no-character"])
                            return
                        }
                        if (buyerCharacter == sellerCharacter) {
                            return
                        }
                        event.isCancelled = true
                        if (sign.getLine(1).startsWith("buy")) {
                            val amount = sign.getLine(1).split(Regex("\\s+"))[1].toInt()
                            val price = sign.getLine(2).split(Regex("\\s+"))[1].toInt()
                            val currencyWords = sign.getLine(2).split(Regex("\\s+"))
                            val currency = currencyProvider.getCurrency(currencyWords.subList(2, currencyWords.size).joinToString(" ")) ?: return
                            val item = event.currentItem ?: return
                            val amtItem = ItemStack(item)
                            amtItem.amount = amount
                            if (chest.blockInventory.containsAtLeast(item, amount)) {
                                if (economyProvider.getBalance(buyerCharacter, currency) >= price) {
                                    economyProvider.setBalance(buyerCharacter, currency, economyProvider.getBalance(buyerCharacter, currency) - price)
                                    if (sellerCharacter != null) {
                                        bankProvider.setBalance(sellerCharacter, currency, bankProvider.getBalance(sellerCharacter, currency) + price)
                                    }
                                    buyerBukkitPlayer.inventory.addItem(amtItem)
                                    chest.blockInventory.removeItem(amtItem)
                                } else {
                                    buyerBukkitPlayer.sendMessage(plugin.messages["not-enough-money"])
                                }
                            } else {
                                buyerBukkitPlayer.sendMessage(plugin.messages["not-enough-shop-items"])
                            }
                        } else if (sign.getLine(1).startsWith("sell")) {
                            event.whoClicked.sendMessage(plugin.messages["no-stealing"])
                        }
                    } else {
                        event.whoClicked.sendMessage(plugin.messages["no-minecraft-profile"])
                    }
                }
            }
        }
    }

}