package com.rpkit.travel.bukkit.listener

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.ChatColor.GREEN


class PlayerInteractListener(private val plugin: RPKTravelBukkit): Listener {

    @EventHandler
    fun onPlayerInteractListener(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        val sign = (clickedBlock.state ?: return) as? Sign ?: return
        if (!sign.getLine(0).equals("$GREEN[warp]", ignoreCase = true)) return
        val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
        val warp = warpProvider.getWarp(sign.getLine(1))
        if (warp != null) {
            event.player.teleport(warp.location)
        }
    }

}