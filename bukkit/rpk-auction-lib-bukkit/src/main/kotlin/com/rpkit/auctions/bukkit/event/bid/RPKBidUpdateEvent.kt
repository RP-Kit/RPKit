package com.rpkit.auctions.bukkit.event.bid

import com.rpkit.auctions.bukkit.bid.RPKBid
import com.rpkit.core.event.RPKEvent

interface RPKBidUpdateEvent: RPKEvent {

    val bid: RPKBid

}