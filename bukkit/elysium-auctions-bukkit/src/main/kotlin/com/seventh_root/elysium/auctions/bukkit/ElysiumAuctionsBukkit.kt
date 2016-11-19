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

package com.seventh_root.elysium.auctions.bukkit

import com.seventh_root.elysium.auctions.bukkit.auction.ElysiumAuctionProviderImpl
import com.seventh_root.elysium.auctions.bukkit.bid.ElysiumBidProviderImpl
import com.seventh_root.elysium.auctions.bukkit.command.auction.AuctionCommand
import com.seventh_root.elysium.auctions.bukkit.command.bid.BidCommand
import com.seventh_root.elysium.auctions.bukkit.database.table.ElysiumAuctionTable
import com.seventh_root.elysium.auctions.bukkit.database.table.ElysiumBidTable
import com.seventh_root.elysium.auctions.bukkit.listener.PlayerInteractListener
import com.seventh_root.elysium.auctions.bukkit.listener.SignChangeListener
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database

/**
 * Elysium auctions plugin default implementation.
 */
class ElysiumAuctionsBukkit: ElysiumBukkitPlugin() {


    override fun onEnable() {
        serviceProviders = arrayOf(
                ElysiumAuctionProviderImpl(this),
                ElysiumBidProviderImpl(this)
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
        database.addTable(ElysiumAuctionTable(database, this))
        database.addTable(ElysiumBidTable(database, this))
    }

}