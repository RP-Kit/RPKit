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

package com.rpkit.auctions.bukkit.listener

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionProvider
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material.AIR
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Player interact listener for auction signs.
 */
class PlayerInteractListener(private val plugin: RPKAuctionsBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock
        if (clickedBlock != null) {
            val sign = clickedBlock.state
            if (sign is Sign) {
                if (sign.getLine(0) == "$GREEN[auction]") {
                    if (event.player.hasPermission("rpkit.auctions.sign.auction.bid")) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                        val auctionProvider = plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
                        if (minecraftProfile != null) {
                            val character = characterProvider.getActiveCharacter(minecraftProfile)
                            val auction = auctionProvider.getAuction(sign.getLine(1).toInt())
                            if (auction != null) {
                                if (System.currentTimeMillis() >= auction.endTime) {
                                    auction.closeBidding()
                                }
                                if (auction.isBiddingOpen) {
                                    val bidAmount = (auction.bids.sortedByDescending(RPKBid::amount).firstOrNull()?.amount?:auction.startPrice) + auction.minimumBidIncrement
                                    if (character != null) {
                                        if (bidAmount < economyProvider.getBalance(character, auction.currency)) {
                                            val radius = plugin.config.getInt("auctions.radius")
                                            val auctionLocation = auction.location
                                            if (radius < 0 || auctionLocation == null || event.player.location.distanceSquared(auctionLocation) <= radius * radius) {
                                                val bid = RPKBidImpl(
                                                        auction = auction,
                                                        character = character,
                                                        amount = bidAmount
                                                )
                                                auction.addBid(bid)
                                                auctionProvider.updateAuction(auction)
                                                event.player.sendMessage(plugin.messages["bid-valid", mapOf(
                                                        Pair("amount", bid.amount.toString()),
                                                        Pair("currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural),
                                                        Pair("item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else "")
                                                )])
                                                auction.bids
                                                        .asSequence()
                                                        .map(RPKBid::character)
                                                        .toSet()
                                                        .asSequence()
                                                        .filter { character -> character != bid.character }
                                                        .mapNotNull(RPKCharacter::minecraftProfile)
                                                        .filter(RPKMinecraftProfile::isOnline)
                                                        .toList()
                                                        .forEach { minecraftProfile ->
                                                            minecraftProfile.sendMessage(plugin.messages["bid-created", mapOf(
                                                                    Pair("auction_id", bid.auction.id.toString()),
                                                                    Pair("character", bid.character.name),
                                                                    Pair("amount", bid.amount.toString()),
                                                                    Pair("currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural),
                                                                    Pair("item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else "")
                                                            )])
                                                        }
                                                sign.setLine(3, (bid.amount + auction.minimumBidIncrement).toString())
                                                sign.update()
                                                if (!auction.isBiddingOpen) {
                                                    clickedBlock.type = AIR
                                                }
                                            } else {
                                                event.player.sendMessage(plugin.messages["bid-invalid-too-far-away"])
                                            }
                                        } else {
                                            event.player.sendMessage(plugin.messages["bid-invalid-not-enough-money"])
                                        }
                                    } else {
                                        event.player.sendMessage(plugin.messages["no-character"])
                                    }
                                } else {
                                    event.player.sendMessage(plugin.messages["bid-invalid-auction-not-open"])
                                }
                            } else {
                                event.player.sendMessage(plugin.messages["bid-invalid-auction-not-existent"])
                            }
                        } else {
                            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                        }
                    } else {
                        event.player.sendMessage(plugin.messages["no-permission-bid"])
                    }
                }
            }
        }
    }

}
