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

package com.rpkit.auctions.bukkit.command.bid

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionProvider
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.Bukkit
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
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.auctions.command.bid")) {
            sender.sendMessage(plugin.messages["no-permission-bid"])
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages["bid-usage"])
            return true
        }
        try {
            val id = args[0].toInt()
            try {
                val bidAmount = args[1].toInt()
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                val auctionProvider = plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile == null) {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                    return true
                }
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                if (character == null) {
                    sender.sendMessage(plugin.messages["no-character"])
                    return true
                }
                val auction = auctionProvider.getAuction(id)
                if (auction == null) {
                    sender.sendMessage(plugin.messages["bid-invalid-auction-not-existent"])
                    return true
                }
                if (System.currentTimeMillis() >= auction.endTime) {
                    auction.closeBidding()
                }
                if (!auction.isBiddingOpen) {
                    sender.sendMessage(plugin.messages["bid-invalid-auction-not-open"])
                    return true
                }
                if (bidAmount >= economyProvider.getBalance(character, auction.currency)) {
                    sender.sendMessage(plugin.messages["bid-invalid-not-enough-money"])
                    return true
                }
                if (bidAmount < (auction.bids.sortedByDescending { bid -> bid.amount }.firstOrNull()?.amount ?: auction.startPrice) + auction.minimumBidIncrement) {
                    sender.sendMessage(plugin.messages["bid-invalid-not-high-enough", mapOf(
                            Pair("amount", ((auction.bids.sortedByDescending(RPKBid::amount).firstOrNull()?.amount ?: auction.startPrice) + auction.minimumBidIncrement).toString())
                    )])
                    return true
                }
                val radius = plugin.config.getInt("auctions.radius")
                val auctionLocation = auction.location
                if (radius >= 0 && auctionLocation != null && sender.location.distanceSquared(auctionLocation) > radius * radius) {
                    sender.sendMessage(plugin.messages["bid-invalid-too-far-away"])
                    return true
                }
                val bid = RPKBidImpl(
                        auction = auction,
                        character = character,
                        amount = bidAmount
                )
                if (!auction.addBid(bid)) {
                    sender.sendMessage(plugin.messages["bid-create-failed"])
                }
                auctionProvider.updateAuction(auction)
                sender.sendMessage(plugin.messages["bid-valid", mapOf(
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
                        .map { minecraftProfile -> Bukkit.getPlayer(minecraftProfile.minecraftUUID) }
                        .filterNotNull()
                        .toList()
                        .forEach { player ->
                            player.sendMessage(plugin.messages["bid-created", mapOf(
                                    Pair("auction_id", bid.auction.id.toString()),
                                    Pair("character", bid.character.name),
                                    Pair("amount", bid.amount.toString()),
                                    Pair("currency", if (bid.amount == 1) auction.currency.nameSingular else auction.currency.namePlural),
                                    Pair("item", auction.item.amount.toString() + " " + auction.item.type.toString().toLowerCase().replace("_", " ") + if (auction.item.amount != 1) "s" else "")
                            )])
                        }
            } catch (exception: NumberFormatException) {
                sender.sendMessage(plugin.messages["bid-invalid-amount-not-a-number"])
            }
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["bid-invalid-auction-id-not-a-number"])
        }
        return true
    }

}