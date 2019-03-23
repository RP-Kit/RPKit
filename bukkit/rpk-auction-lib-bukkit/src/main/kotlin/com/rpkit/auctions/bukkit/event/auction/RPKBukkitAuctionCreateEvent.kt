package com.rpkit.auctions.bukkit.event.auction

import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.core.bukkit.event.RPKBukkitEvent
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class RPKBukkitAuctionCreateEvent(override val auction: RPKAuction) : RPKBukkitEvent(), RPKAuctionCreateEvent, Cancellable {

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }

    private var cancel: Boolean = false

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}