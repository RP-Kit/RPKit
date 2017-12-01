package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockSpreadEvent


class BlockSpreadListener(private val plugin: RPKBlockLoggingBukkit): Listener {

    @EventHandler(priority = MONITOR)
    fun onBlockSpread(event: BlockSpreadEvent) {
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        val blockHistory = blockHistoryProvider.getBlockHistory(event.block)
        val blockChange = RPKBlockChangeImpl(
                blockHistory = blockHistory,
                time = System.currentTimeMillis(),
                profile = null,
                minecraftProfile = null,
                character = null,
                from = event.block.type,
                fromData = event.block.data,
                to = event.newState.type,
                toData = event.newState.rawData,
                reason = "SPREAD"
        )
        blockHistoryProvider.addBlockChange(blockChange)
    }

}