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

package com.rpkit.auctions.bukkit.auction

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidService
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionBiddingCloseEvent
import com.rpkit.auctions.bukkit.event.auction.RPKBukkitAuctionBiddingOpenEvent
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

/**
 * Auction implementation.
 */
class RPKAuctionImpl(
    private val plugin: RPKAuctionsBukkit,
    override var id: RPKAuctionId? = null,
    override val item: ItemStack,
    override val currency: RPKCurrency,
    override val location: RPKLocation?,
    override val character: RPKCharacter,
    override val duration: Long,
    override val endTime: Long,
    override val startPrice: Int,
    override val buyOutPrice: Int?,
    override val noSellPrice: Int?,
    override val minimumBidIncrement: Int,
    override var isBiddingOpen: Boolean = false
) : RPKAuction {

    override val bids: CompletableFuture<List<RPKBid>>
        get() = Services[RPKBidService::class.java]?.getBids(this) ?: CompletableFuture.completedFuture(emptyList())

    override fun addBid(bid: RPKBid): CompletableFuture<Boolean> {
        if (!isBiddingOpen) return CompletableFuture.completedFuture(false)
        return CompletableFuture.supplyAsync {
            val highestCurrentBid = bids.join().maxByOrNull(RPKBid::amount)
            if ((highestCurrentBid == null && bid.amount >= startPrice + minimumBidIncrement) || (highestCurrentBid != null && bid.amount >= highestCurrentBid.amount + minimumBidIncrement)) {
                val bidService = Services[RPKBidService::class.java] ?: return@supplyAsync false
                plugin.server.scheduler.runTask(plugin, Runnable {
                    bidService.addBid(bid)
                })
                if (buyOutPrice != null) {
                    if (bid.amount >= buyOutPrice) {
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            closeBidding()
                        })
                    }
                }
                return@supplyAsync true
            } else {
                return@supplyAsync false
            }
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
            CompletableFuture.runAsync {
                val highestBid = bids.join().maxByOrNull(RPKBid::amount)
                if (highestBid != null) {
                    if (highestBid.amount > noSellPrice ?: 0) {
                        val character = highestBid.character
                        val economyService = Services[RPKEconomyService::class.java]
                        if (economyService != null) {
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                economyService.transfer(character, this.character, currency, highestBid.amount)
                                giveItem(character)
                                val minecraftProfile = character.minecraftProfile
                                if (minecraftProfile != null) {
                                    val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
                                    if (bukkitPlayer.isOnline) {
                                        val characterService = Services[RPKCharacterService::class.java]
                                        if (characterService != null) {
                                            if (characterService.getActiveCharacter(minecraftProfile) == character) {
                                                bukkitPlayer.player?.sendMessage(
                                                    plugin.messages.auctionItemReceived.withParameters(
                                                        amount = item.amount,
                                                        itemType = item.type,
                                                        auctionId = id?.value ?: -1,
                                                        character = this.character
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    } else {
                        plugin.server.scheduler.runTask(plugin, Runnable { giveItemAndSendMessage() })
                    }
                } else {
                    plugin.server.scheduler.runTask(plugin, Runnable { giveItemAndSendMessage() })
                }
                isBiddingOpen = false
            }
        }
    }

    private fun giveItemAndSendMessage() {
        giveItem(character)
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService != null) {
                if (characterService.getActiveCharacter(minecraftProfile) == character) {
                    minecraftProfile.sendMessage(plugin.messages.auctionItemReceived.withParameters(
                        amount = item.amount,
                        itemType = item.type,
                        auctionId = id?.value ?: -1,
                        character = character
                    ))
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
                Services[RPKCharacterService::class.java]?.updateCharacter(character)
            }
        } else {
            val inventoryContentsMutable = character.inventoryContents.toMutableList()
            inventoryContentsMutable.add(item)
            character.inventoryContents = inventoryContentsMutable.toTypedArray()
            Services[RPKCharacterService::class.java]?.updateCharacter(character)
        }
    }

}