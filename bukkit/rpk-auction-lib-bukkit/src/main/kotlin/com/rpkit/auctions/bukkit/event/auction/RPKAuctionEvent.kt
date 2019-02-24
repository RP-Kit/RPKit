package com.rpkit.auctions.bukkit.event.auction

import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.core.event.RPKEvent

interface RPKAuctionEvent: RPKEvent {

    val auction: RPKAuction

}