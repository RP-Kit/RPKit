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
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

/**
 * Sign change listener for auction signs.
 */
class SignChangeListener(private val plugin: RPKAuctionsBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[auction]", ignoreCase = true)) {
            event.setLine(0, "${GREEN}[auction]")
            if (!event.player.hasPermission("rpkit.auctions.sign.auction")) {
                event.block.breakNaturally()
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-auction-sign-create")))
                return
            }
            try {
                val auctionProvider = plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class)
                val auction = auctionProvider.getAuction(event.getLine(1).toInt())
                if (auction == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-sign-invalid-auction-does-not-exist")))
                    return
                } else {
                    event.setLine(2, auction.item.amount.toString() + " x " + auction.item.type.toString().toLowerCase().replace('_', ' '))
                    event.setLine(3, ((auction.bids
                            .sortedByDescending { bid -> bid.amount }
                            .firstOrNull()
                            ?.amount?:auction.startPrice) + auction.minimumBidIncrement).toString())
                }
            } catch (exception: NumberFormatException) {
                event.block.breakNaturally()
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-sign-invalid-id-not-a-number")))
                return
            }
        }
    }

}
