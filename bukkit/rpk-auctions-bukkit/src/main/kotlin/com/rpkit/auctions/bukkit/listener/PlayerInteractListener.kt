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

package com.rpkit.auctions.bukkit.listener

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.auctions.bukkit.bid.RPKBidImpl
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material.AIR
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
            event.player.sendMessage(plugin.messages["no-permission-bid"])
            return
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return
        }
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            event.player.sendMessage(plugin.messages["no-character-service"])
            return
        }
        val economyService = Services[RPKEconomyService::class]
        if (economyService == null) {
            event.player.sendMessage(plugin.messages["no-economy-service"])
            return
        }
        val auctionService = Services[RPKAuctionService::class]
        if (auctionService == null) {
            event.player.sendMessage(plugin.messages["no-auction-service"])
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
        if (minecraftProfile == null) {
            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
            return
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        val auction = auctionService.getAuction(sign.getLine(1).toInt())
        if (auction == null) {
            event.player.sendMessage(plugin.messages["bid-invalid-auction-not-existent"])
            return
        }
        if (System.currentTimeMillis() >= auction.endTime) {
            auction.closeBidding()
        }
        if (!auction.isBiddingOpen) {
            event.player.sendMessage(plugin.messages["bid-invalid-auction-not-open"])
            return
        }
        val bidAmount = (auction.bids.maxBy(RPKBid::amount)?.amount
                ?: auction.startPrice) + auction.minimumBidIncrement
        if (character == null) {
            event.player.sendMessage(plugin.messages["no-character"])
            return
        }
        if (bidAmount >= economyService.getBalance(character, auction.currency)) {
            event.player.sendMessage(plugin.messages["bid-invalid-not-enough-money"])
            return
        }
        val radius = plugin.config.getInt("auctions.radius")
        val auctionLocation = auction.location
        if (radius >= 0 && auctionLocation != null && event.player.location.distanceSquared(auctionLocation) > radius * radius) {
            event.player.sendMessage(plugin.messages["bid-invalid-too-far-away"])
            return
        }
        val bid = RPKBidImpl(
                auction = auction,
                character = character,
                amount = bidAmount
        )
        if (!auction.addBid(bid)) {
            event.player.sendMessage(plugin.messages["bid-create-failed"])
            return
        }
        if (!auctionService.updateAuction(auction)) {
            event.player.sendMessage(plugin.messages["auction-update-failed"])
            return
        }
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
                .filter { it != bid.character }
                .map(RPKCharacter::minecraftProfile)
                .filterNotNull()
                .filter(RPKMinecraftProfile::isOnline)
                .toList()
                .forEach { bidderMinecraftProfile ->
                    bidderMinecraftProfile.sendMessage(plugin.messages["bid-created", mapOf(
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
            event.clickedBlock?.type = AIR
        }
    }

}
