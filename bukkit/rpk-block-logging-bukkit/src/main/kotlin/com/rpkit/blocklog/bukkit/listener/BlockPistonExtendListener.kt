package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent


class BlockPistonExtendListener(private val plugin: RPKBlockLoggingBukkit): Listener {

    @EventHandler(priority = MONITOR)
    fun onBlockPistonExtend(event: BlockPistonExtendEvent) {
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        var block = event.block
        var count = 0
        while (block.type != Material.AIR && count < 12) {
            val blockHistory = blockHistoryProvider.getBlockHistory(block)
            val blockChange = RPKBlockChangeImpl(
                    blockHistory = blockHistory,
                    time = System.currentTimeMillis(),
                    profile = null,
                    minecraftProfile = null,
                    character = null,
                    from = block.type,
                    fromData = block.data,
                    to = block.getRelative(event.direction.oppositeFace).type,
                    toData = block.getRelative(event.direction.oppositeFace).data,
                    reason = "PISTON"
            )
            blockHistoryProvider.addBlockChange(blockChange)
            block = block.getRelative(event.direction)
            count++
        }
    }

}