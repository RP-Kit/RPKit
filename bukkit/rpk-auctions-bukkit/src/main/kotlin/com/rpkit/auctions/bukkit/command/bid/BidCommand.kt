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

package com.rpkit.auctions.bukkit.command.bid

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionId
import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for bidding on auctions.
 * Player must specify the auction ID (given to the creator of the auction upon creation) and the amount bid.
 */
class BidCommand(private val plugin: RPKAuctionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.auctions.command.bid")) {
            sender.sendMessage(plugin.messages.noPermissionBid)
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.bidUsage)
            return true
        }
        try {
            val id = args[0].toInt()
            try {
                val bidAmount = args[1].toInt()
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfileService)
                    return true
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    sender.sendMessage(plugin.messages.noCharacterService)
                    return true
                }
                val economyService = Services[RPKEconomyService::class.java]
                if (economyService == null) {
                    sender.sendMessage(plugin.messages.noEconomyService)
                    return true
                }
                val auctionService = Services[RPKAuctionService::class.java]
                if (auctionService == null) {
                    sender.sendMessage(plugin.messages.noAuctionService)
                    return true
                }
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
                if (minecraftProfile == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfile)
                    return true
                }
                val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return true
                }
                auctionService.getAuction(RPKAuctionId(id))
                    .thenAccept getAuction@{ auction ->
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            if (auction == null) {
                                sender.sendMessage(plugin.messages.bidInvalidAuctionNotExistent)
                                return@Runnable
                            }
                            if (System.currentTimeMillis() >= auction.endTime) {
                                auction.closeBidding()
                                auctionService.updateAuction(auction)
                            }
                            if (!auction.isBiddingOpen) {
                                sender.sendMessage(plugin.messages.bidInvalidAuctionNotOpen)
                                return@Runnable
                            }
                            val walletBalance = economyService.getPreloadedBalance(character, auction.currency)
                            if (walletBalance == null) {
                                sender.sendMessage(plugin.messages.noPreloadedBalance)
                                return@Runnable
                            }
                            if (bidAmount > walletBalance) {
                                sender.sendMessage(plugin.messages.bidInvalidNotEnoughMoney)
                                return@Runnable
                            }
                            auction.bids.thenAccept getBids@{ bids ->
                                if (bidAmount < (bids.maxByOrNull(RPKBid::amount)?.amount
                                        ?: auction.startPrice) + auction.minimumBidIncrement) {
                                    sender.sendMessage(plugin.messages.bidInvalidNotHighEnough.withParameters(
                                        amount = (bids.maxByOrNull(RPKBid::amount)?.amount ?: auction.startPrice)
                                                + auction.minimumBidIncrement
                                    ))
                                    return@getBids
                                }
                                val radius = plugin.config.getInt("auctions.radius")
                                val auctionLocation = auction.location
                                if (radius >= 0 && auctionLocation != null && sender.location.distanceSquared(auctionLocation) > radius * radius) {
                                    sender.sendMessage(plugin.messages.bidInvalidTooFarAway)
                                    return@getBids
                                }
                                val bid = RPKBidImpl(
                                    auction = auction,
                                    character = character,
                                    amount = bidAmount
                                )
                                plugin.server.scheduler.runTask(plugin, Runnable {
                                    auction.addBid(bid).thenAccept addBid@{ bidSuccessful ->
                                        if (!bidSuccessful) {
                                            sender.sendMessage(plugin.messages.bidCreateFailed)
                                        } else {
                                            plugin.server.scheduler.runTask(plugin, Runnable {
                                                auctionService.updateAuction(auction).thenAccept updateAuction@{
                                                    sender.sendMessage(plugin.messages.bidValid.withParameters(
                                                        currencyAmount = bid.amount,
                                                        currency = auction.currency,
                                                        itemType = auction.item.type,
                                                        itemAmount = auction.item.amount
                                                    ))
                                                    auction.bids
                                                        .thenAccept newBids@{ newBids ->
                                                            newBids
                                                                .map(RPKBid::character)
                                                                .mapNotNull(RPKCharacter::minecraftProfile)
                                                                .distinct()
                                                                .filter { it != minecraftProfile }
                                                                .forEach { bidderMinecraftProfile ->
                                                                    bidderMinecraftProfile.sendMessage(plugin.messages.bidCreated.withParameters(
                                                                        auction = bid.auction,
                                                                        character = bid.character,
                                                                        currencyAmount = bid.amount,
                                                                        currency = auction.currency,
                                                                        itemAmount = auction.item.amount,
                                                                        itemType = auction.item.type
                                                                    ))
                                                                }
                                                        }
                                                }
                                            })
                                        }
                                    }
                                })
                            }
                        })
                }
            } catch (exception: NumberFormatException) {
                sender.sendMessage(plugin.messages.bidInvalidAmountNotANumber)
            }
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages.bidInvalidAuctionIdNotANumber)
        }
        return true
    }

}