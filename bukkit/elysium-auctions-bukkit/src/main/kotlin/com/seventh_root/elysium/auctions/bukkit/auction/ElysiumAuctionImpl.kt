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

package com.seventh_root.elysium.auctions.bukkit.auction

import com.seventh_root.elysium.auctions.bukkit.ElysiumAuctionsBukkit
import com.seventh_root.elysium.auctions.bukkit.bid.ElysiumBid
import com.seventh_root.elysium.auctions.bukkit.bid.ElysiumBidProvider
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

/**
 * Auction implementation.
 */
class ElysiumAuctionImpl(
        private val plugin: ElysiumAuctionsBukkit,
        override var id: Int = 0,
        override val item: ItemStack,
        override val currency: ElysiumCurrency,
        override val location: Location?,
        override val character: ElysiumCharacter,
        override val duration: Long,
        override val endTime: Long,
        override val startPrice: Int,
        override val buyOutPrice: Int?,
        override val noSellPrice: Int?,
        override val minimumBidIncrement: Int,
        override var isBiddingOpen: Boolean = false
) : ElysiumAuction {

    override val bids: List<ElysiumBid>
        get() = plugin.core.serviceManager.getServiceProvider(ElysiumBidProvider::class).getBids(this)

    override fun addBid(bid: ElysiumBid) {
        if (!isBiddingOpen) return
        val highestCurrentBid = bids.sortedByDescending { bid -> bid.amount }.firstOrNull()
        if ((highestCurrentBid == null && bid.amount >= startPrice + minimumBidIncrement) || (highestCurrentBid != null && bid.amount >= highestCurrentBid.amount + minimumBidIncrement)) {
            plugin.core.serviceManager.getServiceProvider(ElysiumBidProvider::class).addBid(bid)
            if (buyOutPrice != null) {
                if (bid.amount >= buyOutPrice) {
                    closeBidding()
                }
            }
        }
    }

    override fun openBidding() {
        if (!isBiddingOpen) {
            isBiddingOpen = true
        } else {
            throw IllegalStateException("Bidding is already open.")
        }
    }

    override fun closeBidding() {
        if (isBiddingOpen) {
            val highestBid = bids.sortedByDescending { bid -> bid.amount }.firstOrNull()
            if (highestBid != null) {
                if (highestBid.amount > noSellPrice?:0) {
                    val character = highestBid.character
                    val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
                    economyProvider.transfer(character, this.character, currency, highestBid.amount)
                    giveItem(character)
                    val player = character.player
                    if (player != null) {
                        val bukkitPlayer = player.bukkitPlayer
                        if (bukkitPlayer != null) {
                            if (bukkitPlayer.isOnline) {
                                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                                if (characterProvider.getActiveCharacter(player) == character) {
                                    bukkitPlayer.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-item-received"))
                                            .replace("\$amount", item.amount.toString())
                                            .replace("\$item", item.type.toString().toLowerCase().replace("_", "") + if (item.amount != 1) "s" else "")
                                            .replace("\$character", this.character.name)
                                            .replace("\$auction_id", id.toString())
                                    )
                                }
                            }
                        }
                    }
                } else {
                    giveItem(character)
                    val player = character.player
                    if (player != null) {
                        val bukkitPlayer = player.bukkitPlayer
                        if (bukkitPlayer != null) {
                            if (bukkitPlayer.isOnline) {
                                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                                if (characterProvider.getActiveCharacter(player) == character) {
                                    bukkitPlayer.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-item-received"))
                                            .replace("\$amount", item.amount.toString())
                                            .replace("\$item", item.type.toString().toLowerCase().replace("_", "") + if (item.amount != 1) "s" else "")
                                            .replace("\$character", this.character.name)
                                            .replace("\$auction_id", id.toString())
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                giveItem(character)
                val player = character.player
                if (player != null) {
                    val bukkitPlayer = player.bukkitPlayer
                    if (bukkitPlayer != null) {
                        if (bukkitPlayer.isOnline) {
                            val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                            if (characterProvider.getActiveCharacter(player) == character) {
                                bukkitPlayer.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-item-received"))
                                        .replace("\$amount", item.amount.toString())
                                        .replace("\$item", item.type.toString().toLowerCase().replace("_", "") + if (item.amount != 1) "s" else "")
                                        .replace("\$character", this.character.name)
                                        .replace("\$auction_id", id.toString())
                                )
                            }
                        }
                    }
                }
            }
            isBiddingOpen = false
        }
    }

    private fun giveItem(character: ElysiumCharacter) {
        val player = character.player
        if (player != null) {
            val bukkitPlayer = player.bukkitPlayer
            if (bukkitPlayer != null) {
                if (bukkitPlayer.isOnline) {
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                    if (characterProvider.getActiveCharacter(player) == character) {
                        bukkitPlayer.player.inventory.addItem(item).values.forEach { item ->
                            bukkitPlayer.player.world.dropItem(bukkitPlayer.player.location, item)
                        }
                    } else {
                        val inventoryContentsMutable = character.inventoryContents.toMutableList()
                        inventoryContentsMutable.add(item)
                        character.inventoryContents = inventoryContentsMutable.toTypedArray()
                        characterProvider.updateCharacter(character)
                    }
                } else {
                    val inventoryContentsMutable = character.inventoryContents.toMutableList()
                    inventoryContentsMutable.add(item)
                    character.inventoryContents = inventoryContentsMutable.toTypedArray()
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                    characterProvider.updateCharacter(character)
                }
            } else {
                val inventoryContentsMutable = character.inventoryContents.toMutableList()
                inventoryContentsMutable.add(item)
                character.inventoryContents = inventoryContentsMutable.toTypedArray()
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                characterProvider.updateCharacter(character)
            }
        } else {
            val inventoryContentsMutable = character.inventoryContents.toMutableList()
            inventoryContentsMutable.add(item)
            character.inventoryContents = inventoryContentsMutable.toTypedArray()
            val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
            characterProvider.updateCharacter(character)
        }
    }

}