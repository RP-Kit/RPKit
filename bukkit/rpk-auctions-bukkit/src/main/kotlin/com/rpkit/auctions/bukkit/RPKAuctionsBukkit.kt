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

package com.rpkit.auctions.bukkit

import com.rpkit.auctions.bukkit.auction.RPKAuctionProviderImpl
import com.rpkit.auctions.bukkit.bid.RPKBidProviderImpl
import com.rpkit.auctions.bukkit.command.auction.AuctionCommand
import com.rpkit.auctions.bukkit.command.bid.BidCommand
import com.rpkit.auctions.bukkit.database.table.RPKAuctionTable
import com.rpkit.auctions.bukkit.database.table.RPKBidTable
import com.rpkit.auctions.bukkit.listener.PlayerInteractListener
import com.rpkit.auctions.bukkit.listener.SignChangeListener
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database

/**
 * RPK auctions plugin default implementation.
 */
class RPKAuctionsBukkit: RPKBukkitPlugin() {


    override fun onEnable() {
        serviceProviders = arrayOf(
                RPKAuctionProviderImpl(this),
                RPKBidProviderImpl(this)
        )
    }

    override fun registerCommands() {
        getCommand("auction").executor = AuctionCommand(this)
        getCommand("bid").executor = BidCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                SignChangeListener(this),
                PlayerInteractListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKAuctionTable(database, this))
        database.addTable(RPKBidTable(database, this))
    }

}