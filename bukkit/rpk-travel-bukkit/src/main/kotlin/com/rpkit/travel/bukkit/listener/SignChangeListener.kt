package com.rpkit.travel.bukkit.listener

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpProvider
import org.bukkit.ChatColor.GREEN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent


class SignChangeListener(private val plugin: RPKTravelBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[warp]", ignoreCase = true)) {
            if (event.player.hasPermission("rpkit.travel.sign.warp.create")) {
                val warpProvider = plugin.core.serviceManager.getServiceProvider(RPKWarpProvider::class)
                val warp = warpProvider.getWarp(event.getLine(1) ?: "")
                if (warp == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.messages["warp-sign-invalid-warp"])
                    return
                }
                event.setLine(0, "$GREEN[warp]")
                event.player.sendMessage(plugin.messages["warp-sign-valid"])
            } else {
                event.player.sendMessage(plugin.messages["no-permission-warp-sign-create"])
            }
        }
    }

}