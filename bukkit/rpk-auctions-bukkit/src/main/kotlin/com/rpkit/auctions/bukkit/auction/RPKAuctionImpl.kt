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

package com.rpkit.auctions.bukkit.auction

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidService
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionBiddingCloseEvent
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionBiddingOpenEvent
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

/**
 * Auction implementation.
 */
class RPKAuctionImpl(
        private val plugin: RPKAuctionsBukkit,
        override var id: Int? = null,
        override val item: ItemStack,
        override val currency: RPKCurrency,
        override val location: Location?,
        override val character: RPKCharacter,
        override val duration: Long,
        override val endTime: Long,
        override val startPrice: Int,
        override val buyOutPrice: Int?,
        override val noSellPrice: Int?,
        override val minimumBidIncrement: Int,
        override var isBiddingOpen: Boolean = false
) : RPKAuction {

    override val bids: List<RPKBid>
        get() = Services[RPKBidService::class.java]?.getBids(this) ?: emptyList()

    override fun addBid(bid: RPKBid): Boolean {
        if (!isBiddingOpen) return false
        val highestCurrentBid = bids.maxBy(RPKBid::amount)
        if ((highestCurrentBid == null && bid.amount >= startPrice + minimumBidIncrement) || (highestCurrentBid != null && bid.amount >= highestCurrentBid.amount + minimumBidIncrement)) {
            val bidService = Services[RPKBidService::class.java] ?: return false
            if (!bidService.addBid(bid)) {
                return false
            }
            if (buyOutPrice != null) {
                if (bid.amount >= buyOutPrice) {
                    closeBidding()
                }
            }
            return true
        } else {
            return false
        }
    }

    override fun openBidding() {
        if (!isBiddingOpen) {
            val event = RPKBukkitAuctionBiddingOpenEvent(this)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            isBiddingOpen = true
        } else {
            throw IllegalStateException("Bidding is already open.")
        }
    }

    override fun closeBidding() {
        if (isBiddingOpen) {
            val event = RPKBukkitAuctionBiddingCloseEvent(this)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            val highestBid = bids.maxBy(RPKBid::amount)
            if (highestBid != null) {
                if (highestBid.amount > noSellPrice ?: 0) {
                    val character = highestBid.character
                    val economyService = Services[RPKEconomyService::class.java]
                    if (economyService != null) {
                        economyService.transfer(character, this.character, currency, highestBid.amount)
                        giveItem(character)
                        val minecraftProfile = character.minecraftProfile
                        if (minecraftProfile != null) {
                            val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
                            if (bukkitPlayer.isOnline) {
                                val characterService = Services[RPKCharacterService::class.java]
                                if (characterService != null) {
                                    if (characterService.getActiveCharacter(minecraftProfile) == character) {
                                        bukkitPlayer.player?.sendMessage(plugin.messages["auction-item-received", mapOf(
                                                Pair("amount", item.amount.toString()),
                                                Pair("item", item.type.toString().toLowerCase().replace("_", "") + if (item.amount != 1) "s" else ""),
                                                Pair("character", this.character.name),
                                                Pair("auction_id", id.toString())
                                        )])
                                    }
                                }
                            }
                        }
                    }
                } else {
                    giveItemAndSendMessage()
                }
            } else {
                giveItemAndSendMessage()
            }
            isBiddingOpen = false
        }
    }

    private fun giveItemAndSendMessage() {
        giveItem(character)
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService != null) {
                if (characterService.getActiveCharacter(minecraftProfile) == character) {
                    minecraftProfile.sendMessage(plugin.messages["auction-item-received", mapOf(
                            Pair("amount", item.amount.toString()),
                            Pair("item", item.type.toString().toLowerCase().replace("_", "") + if (item.amount != 1) "s" else ""),
                            Pair("character", this.character.name),
                            Pair("auction_id", id.toString())
                    )])
                }
            }
        }
    }

    private fun giveItem(character: RPKCharacter) {
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitOnlinePlayer = bukkitPlayer.player
            if (bukkitOnlinePlayer != null) {
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService != null) {
                    if (characterService.getActiveCharacter(minecraftProfile) == character) {
                        bukkitOnlinePlayer.inventory.addItem(item).values.forEach { item ->
                            bukkitOnlinePlayer.world.dropItem(bukkitOnlinePlayer.location, item)
                        }
                    } else {
                        val inventoryContentsMutable = character.inventoryContents.toMutableList()
                        inventoryContentsMutable.add(item)
                        character.inventoryContents = inventoryContentsMutable.toTypedArray()
                        characterService.updateCharacter(character)
                    }
                }
            } else {
                val inventoryContentsMutable = character.inventoryContents.toMutableList()
                inventoryContentsMutable.add(item)
                character.inventoryContents = inventoryContentsMutable.toTypedArray()
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService != null) {
                    characterService.updateCharacter(character)
                }
            }
        } else {
            val inventoryContentsMutable = character.inventoryContents.toMutableList()
            inventoryContentsMutable.add(item)
            character.inventoryContents = inventoryContentsMutable.toTypedArray()
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService != null) {
                characterService.updateCharacter(character)
            }
        }
    }

}