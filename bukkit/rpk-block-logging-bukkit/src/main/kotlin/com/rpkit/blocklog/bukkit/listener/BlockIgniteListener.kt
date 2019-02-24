package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent


class BlockIgniteListener(private val plugin: RPKBlockLoggingBukkit): Listener {

    @EventHandler(priority = MONITOR)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        val player = event.player
        val minecraftProfile = if (player == null) null else minecraftProfileProvider.getMinecraftProfile(player)
        val profile = minecraftProfile?.profile
        val character = if (minecraftProfile == null) null else characterProvider.getActiveCharacter(minecraftProfile)
        val blockHistory = blockHistoryProvider.getBlockHistory(event.block)
        val blockChange = RPKBlockChangeImpl(
                blockHistory = blockHistory,
                time = System.currentTimeMillis(),
                profile = profile,
                minecraftProfile = minecraftProfile,
                character = character,
                from = event.block.type,
                to = event.block.type,
                reason = "IGNITE"
        )
        blockHistoryProvider.addBlockChange(blockChange)
    }

}