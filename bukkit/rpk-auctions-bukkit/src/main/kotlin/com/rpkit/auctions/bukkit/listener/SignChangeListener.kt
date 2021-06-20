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
import com.rpkit.core.service.Services
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

/**
 * Sign change listener for auction signs.
 */
class SignChangeListener(private val plugin: RPKAuctionsBukkit) : Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[auction]", ignoreCase = true)) {
            event.setLine(0, "$GREEN[auction]")
            if (!event.player.hasPermission("rpkit.auctions.sign.auction")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages.noPermissionAuctionSignCreate)
                return
            }
            try {
                val auctionService = Services[RPKAuctionService::class.java]
                if (auctionService == null) {
                    event.player.sendMessage(plugin.messages.noAuctionService)
                    return
                }
                val auctionId = event.getLine(1)?.toInt()
                if (auctionId == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.messages.auctionSignInvalidIdNotANumber)
                    return
                }
                auctionService.getAuction(RPKAuctionId(auctionId)).thenAccept { auction ->
                    if (auction == null) {
                        event.block.breakNaturally()
                        event.player.sendMessage(plugin.messages.auctionSignInvalidAuctionDoesNotExist)
                        return@thenAccept
                    } else {
                        auction.bids.thenAccept { bids ->
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                val data = event.block.state
                                if (data is Sign) {
                                    data.setLine(2,
                                        auction.item.amount.toString() + " x " + auction.item.type.toString()
                                            .toLowerCase().replace('_', ' ')
                                    )
                                    data.setLine(
                                        3, ((bids.maxByOrNull(RPKBid::amount)
                                            ?.amount ?: auction.startPrice) + auction.minimumBidIncrement).toString()
                                    )
                                    data.update()
                                }
                            })
                        }
                    }
                }
            } catch (exception: NumberFormatException) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages.auctionSignInvalidIdNotANumber)
                return
            }
        }
    }

}
