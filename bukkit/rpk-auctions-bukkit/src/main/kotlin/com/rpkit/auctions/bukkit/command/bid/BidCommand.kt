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

package com.rpkit.auctions.bukkit.command.bid

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionProvider
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for bidding on auctions.
 * Player must specify the auction ID (given to the creator of the auction upon creation) and the amount bid.
 */
class BidCommand(private val plugin: RPKAuctionsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.auctions.command.bid")) {
                if (args.size > 1) {
                    try {
                        val id = args[0].toInt()
                        try {
                            val bidAmount = args[1].toInt()
                            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                            val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                            val auctionProvider = plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class)
                            val player = playerProvider.getPlayer(sender)
                            val character = characterProvider.getActiveCharacter(player)
                            if (character != null) {
                                val auction = auctionProvider.getAuction(id)
                                if (auction != null) {
                                    if (System.currentTimeMillis() >= auction.endTime) {
                                        auction.closeBidding()
                                    }
                                    if (auction.isBiddingOpen) {
                                        if (bidAmount < economyProvider.getBalance(character, auction.currency)) {
                                            if (bidAmount >= (auction.bids.sortedByDescending { bid -> bid.amount }.firstOrNull()?.amount?:auction.startPrice) + auction.minimumBidIncrement) {
                                                val radius = plugin.config.getInt("auctions.radius")
                                                if (radius < 0 || sender.location.distanceSquared(auction.location) <= radius * radius) {
                                                    val bid = RPKBidImpl(
                                                            auction = auction,
                                                            character = character,
                                                            amount = bidAmount
                                                    )
                                                    auction.addBid(bid)
                                                    auctionProvider.updateAuction(auction)
                                                    sender.sendMessage(plugin.core.messages["bid-valid", mapOf(
                                                            Pair("amount", bid.amount.toString()),
                                                            Pair("currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural),
                                                            Pair("item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else "")
                                                    )])
                                                    auction.bids
                                                            .map(RPKBid::character)
                                                            .toSet()
                                                            .filter { character -> character != bid.character }
                                                            .filter { character -> character.player != null }
                                                            .map { character -> character.player!! }
                                                            .filter { player -> player.bukkitPlayer != null }
                                                            .map { player -> player.bukkitPlayer!! }
                                                            .filter { bukkitPlayer -> bukkitPlayer.isOnline }
                                                            .map { bukkitPlayer -> bukkitPlayer.player }
                                                            .forEach { player ->
                                                                player.sendMessage(plugin.core.messages["bid-created", mapOf(
                                                                        Pair("auction_id", bid.auction.id.toString()),
                                                                        Pair("character", bid.character.name),
                                                                        Pair("amount", bid.amount.toString()),
                                                                        Pair("currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural),
                                                                        Pair("item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else "")
                                                                )])
                                                            }
                                                } else {
                                                    sender.sendMessage(plugin.core.messages["bid-invalid-too-far-away"])
                                                }
                                            } else {
                                                sender.sendMessage(plugin.core.messages["bid-invalid-not-high-enough", mapOf(
                                                        Pair("amount", ((auction.bids.sortedByDescending(RPKBid::amount).firstOrNull()?.amount?:auction.startPrice) + auction.minimumBidIncrement).toString())
                                                )])
                                            }
                                        } else {
                                            sender.sendMessage(plugin.core.messages["bid-invalid-not-enough-money"])
                                        }
                                    } else {
                                        sender.sendMessage(plugin.core.messages["bid-invalid-auction-not-open"])
                                    }
                                } else {
                                    sender.sendMessage(plugin.core.messages["bid-invalid-auction-not-existent"])
                                }
                            } else {
                                sender.sendMessage(plugin.core.messages["no-character"])
                            }
                        } catch (exception: NumberFormatException) {
                            sender.sendMessage(plugin.core.messages["bid-invalid-amount-not-a-number"])
                        }
                    } catch (exception: NumberFormatException) {
                        sender.sendMessage(plugin.core.messages["bid-invalid-auction-id-not-a-number"])
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["bid-usage"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["no-permission-bid"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["not-from-console"])
        }
        return true
    }

}