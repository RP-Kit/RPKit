package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent


class EntityExplodeListener(private val plugin: RPKBlockLoggingBukkit): Listener {

    @EventHandler(priority = MONITOR)
    fun onEntityExplode(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
            val blockHistory = blockHistoryProvider.getBlockHistory(block)
            val blockChange = RPKBlockChangeImpl(
                    blockHistory = blockHistory,
                    time = System.currentTimeMillis(),
                    profile = null,
                    minecraftProfile = null,
                    character = null,
                    from = block.type,
                    fromData = block.data,
                    to = Material.AIR,
                    toData = 0,
                    reason = "ENTITY_EXPLODE"
            )
            blockHistoryProvider.addBlockChange(blockChange)
        }
    }

}