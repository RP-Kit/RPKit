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

package com.seventh_root.elysium.auctions.bukkit.listener

import com.seventh_root.elysium.auctions.bukkit.ElysiumAuctionsBukkit
import com.seventh_root.elysium.auctions.bukkit.auction.ElysiumAuctionProvider
import com.seventh_root.elysium.auctions.bukkit.bid.ElysiumBidImpl
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material.AIR
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener(private val plugin: ElysiumAuctionsBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasBlock()) {
            val sign = event.clickedBlock.state
            if (sign is Sign) {
                if (sign.getLine(0).equals("$GREEN[auction]")) {
                    if (event.player.hasPermission("elysium.auctions.sign.auction.bid")) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
                        val auctionProvider = plugin.core.serviceManager.getServiceProvider(ElysiumAuctionProvider::class)
                        val player = playerProvider.getPlayer(event.player)
                        val character = characterProvider.getActiveCharacter(player)
                        val auction = auctionProvider.getAuction(sign.getLine(1).toInt())
                        if (auction != null) {
                            if (System.currentTimeMillis() >= auction.endTime) {
                                auction.closeBidding()
                            }
                            if (auction.isBiddingOpen) {
                                val bidAmount = (auction.bids.sortedByDescending { bid -> bid.amount }.firstOrNull()?.amount?:auction.startPrice) + auction.minimumBidIncrement
                                if (character != null) {
                                    if (bidAmount < economyProvider.getBalance(character, auction.currency)) {
                                        val radius = plugin.config.getInt("auctions.radius")
                                        if (radius < 0 || event.player.location.distanceSquared(auction.location) <= radius * radius) {
                                            val bid = ElysiumBidImpl(
                                                    auction = auction,
                                                    character = character,
                                                    amount = bidAmount
                                            )
                                            auction.addBid(bid)
                                            auctionProvider.updateAuction(auction)
                                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid-valid"))
                                                    .replace("\$amount", bid.amount.toString())
                                                    .replace("\$currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural)
                                                    .replace("\$item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else ""))
                                            auction.bids
                                                    .map { bid -> bid.character }
                                                    .toSet()
                                                    .filter { character -> character != bid.character }
                                                    .filter { character -> character.player != null }
                                                    .map { character -> character.player!! }
                                                    .filter { player -> player.bukkitPlayer != null }
                                                    .map { player -> player.bukkitPlayer!! }
                                                    .filter { bukkitPlayer -> bukkitPlayer.isOnline }
                                                    .map { bukkitPlayer -> bukkitPlayer.player }
                                                    .forEach { player ->
                                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid-created"))
                                                                .replace("\$auction_id", bid.auction.id.toString())
                                                                .replace("\$character", bid.character.name)
                                                                .replace("\$amount", bid.amount.toString())
                                                                .replace("\$currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural)
                                                                .replace("\$item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else ""))
                                                    }
                                            sign.setLine(3, (bid.amount + auction.minimumBidIncrement).toString())
                                            sign.update()
                                            if (!auction.isBiddingOpen) {
                                                event.clickedBlock.type = AIR
                                            }
                                        } else {
                                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid-invalid-too-far-away")))
                                        }
                                    } else {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid-invalid-not-enough-money")))
                                    }
                                } else {
                                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                                }
                            } else {
                                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid-invalid-auction-not-open")))
                            }
                        } else {
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid-invalid-auction-not-existent")))
                        }
                    } else {
                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bid")))
                    }
                }
            }
        }
    }

}
