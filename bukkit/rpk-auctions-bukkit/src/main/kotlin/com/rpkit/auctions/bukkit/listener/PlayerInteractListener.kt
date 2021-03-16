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

package com.rpkit.auctions.bukkit.listener

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionId
import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Player interact listener for auction signs.
 */
class PlayerInteractListener(private val plugin: RPKAuctionsBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.hasBlock()) return
        val sign = event.clickedBlock?.state as? Sign ?: return
        if (sign.getLine(0) != "$GREEN[auction]") return
        if (!event.player.hasPermission("rpkit.auctions.sign.auction.bid")) {
            event.player.sendMessage(plugin.messages.noPermissionBid)
            return
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            event.player.sendMessage(plugin.messages.noMinecraftProfileService)
            return
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            event.player.sendMessage(plugin.messages.noCharacterService)
            return
        }
        val economyService = Services[RPKEconomyService::class.java]
        if (economyService == null) {
            event.player.sendMessage(plugin.messages.noEconomyService)
            return
        }
        val auctionService = Services[RPKAuctionService::class.java]
        if (auctionService == null) {
            event.player.sendMessage(plugin.messages.noAuctionService)
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
        if (minecraftProfile == null) {
            event.player.sendMessage(plugin.messages.noMinecraftProfile)
            return
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        auctionService.getAuction(RPKAuctionId(sign.getLine(1).toInt())).thenAccept getAuction@{ auction ->
            if (auction == null) {
                event.player.sendMessage(plugin.messages.bidInvalidAuctionNotExistent)
                return@getAuction
            }
            if (System.currentTimeMillis() >= auction.endTime) {
                auction.closeBidding()
            }
            if (!auction.isBiddingOpen) {
                event.player.sendMessage(plugin.messages.bidInvalidAuctionNotOpen)
                return@getAuction
            }
            auction.bids.thenAccept getBids@{ bids ->
                val bidAmount = (bids.maxByOrNull(RPKBid::amount)?.amount
                    ?: auction.startPrice) + auction.minimumBidIncrement
                if (character == null) {
                    event.player.sendMessage(plugin.messages.noCharacter)
                    return@getBids
                }
                if (bidAmount > economyService.getBalance(character, auction.currency)) {
                    event.player.sendMessage(plugin.messages.bidInvalidNotEnoughMoney)
                    return@getBids
                }
                val radius = plugin.config.getInt("auctions.radius")
                val auctionLocation = auction.location
                if (radius >= 0 && auctionLocation != null && event.player.location.distanceSquared(auctionLocation) > radius * radius) {
                    event.player.sendMessage(plugin.messages.bidInvalidTooFarAway)
                    return@getBids
                }
                val bid = RPKBidImpl(
                    auction = auction,
                    character = character,
                    amount = bidAmount
                )
                auction.addBid(bid).thenAccept addBid@{ bidSuccessful ->
                    if (!bidSuccessful) {
                        event.player.sendMessage(plugin.messages.bidCreateFailed)
                        return@addBid
                    }
                    auctionService.updateAuction(auction).thenAccept updateAuction@{ auctionUpdateSuccessful ->
                        if (!auctionUpdateSuccessful) {
                            event.player.sendMessage(plugin.messages.auctionUpdateFailed)
                            return@updateAuction
                        }
                        event.player.sendMessage(plugin.messages.bidValid.withParameters(
                            currencyAmount = bid.amount,
                            currency = auction.currency,
                            itemAmount = auction.item.amount,
                            itemType = auction.item.type
                        ))
                        auction.bids.thenAccept newBids@{ bids ->
                            bids.asSequence()
                                .map(RPKBid::character)
                                .toSet()
                                .asSequence()
                                .filter { it != bid.character }
                                .map(RPKCharacter::minecraftProfile)
                                .filterNotNull()
                                .filter(RPKMinecraftProfile::isOnline)
                                .toList()
                                .forEach { bidderMinecraftProfile ->
                                    bidderMinecraftProfile.sendMessage(plugin.messages.bidCreated.withParameters(
                                        auction = bid.auction,
                                        character = bid.character,
                                        currencyAmount = bid.amount,
                                        currency = auction.currency,
                                        itemType = auction.item.type,
                                        itemAmount = auction.item.amount
                                    ))
                                }
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                sign.setLine(3, (bid.amount + auction.minimumBidIncrement).toString())
                                sign.update()
                                if (!auction.isBiddingOpen) {
                                    event.clickedBlock?.type = Material.AIR
                                }
                            })
                        }
                    }
                }
            }
        }
    }

}
