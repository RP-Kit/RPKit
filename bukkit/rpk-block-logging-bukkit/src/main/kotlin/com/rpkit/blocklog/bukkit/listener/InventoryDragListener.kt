package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChangeImpl
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.scheduler.BukkitRunnable

class InventoryDragListener(private val plugin: RPKBlockLoggingBukkit): Listener {

    @EventHandler(priority = MONITOR)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        val inventoryHolder = event.inventory.holder
        if (inventoryHolder is BlockState) {
            val whoClicked = event.whoClicked
            if (whoClicked is Player) {
                val oldContents = event.inventory.contents
                object: BukkitRunnable() {
                    override fun run() {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(whoClicked)
                        val profile = minecraftProfile?.profile
                        val character = if (minecraftProfile == null) null else characterProvider.getActiveCharacter(minecraftProfile)
                        val blockHistory = blockHistoryProvider.getBlockHistory(inventoryHolder.block)
                        val blockInventoryChange = RPKBlockInventoryChangeImpl(
                                blockHistory = blockHistory,
                                time = System.currentTimeMillis(),
                                profile = profile,
                                minecraftProfile = minecraftProfile,
                                character = character,
                                from = oldContents,
                                to = event.inventory.contents,
                                reason = "DRAG"
                        )
                        blockHistoryProvider.addBlockInventoryChange(blockInventoryChange)
                    }
                }.runTaskLater(plugin, 1L) // Scheduled 1 tick later to allow inventory change to take place.
            }
        }
    }

}
