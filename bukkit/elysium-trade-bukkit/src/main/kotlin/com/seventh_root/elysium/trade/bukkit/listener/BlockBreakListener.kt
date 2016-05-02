package com.seventh_root.elysium.trade.bukkit.listener

import com.seventh_root.elysium.trade.bukkit.ElysiumTradeBukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent


class BlockBreakListener(private val plugin: ElysiumTradeBukkit): Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block != null) {
            if (event.block.state != null) {
                if (event.block.state is Sign) {
                    val sign = event.block.state as Sign
                    if (sign.getLine(0).equals(GREEN.toString() + "[trader]")) {
                        if (!event.player.hasPermission("elysium.trade.sign.trader.destroy")) {
                            event.isCancelled = true
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-trader-destroy")))
                        }
                    }
                }
            }
        }
    }

}