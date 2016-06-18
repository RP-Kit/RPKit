package com.seventh_root.elysium.trade.bukkit.listener

import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.trade.bukkit.ElysiumTradeBukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent


class SignChangeListener(private val plugin: ElysiumTradeBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[trader]")) {
            if (event.player.hasPermission("elysium.trade.sign.trader.create")) {
                if (Material.matchMaterial(event.getLine(1)) == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sign-invalid-material")))
                    return
                }
                if (!event.getLine(2).matches(Regex("\\d+ \\| \\d+"))) {
                    event.block.breakNaturally()
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sign-invalid-price")))
                    return
                }
                if (plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java).getCurrency(event.getLine(3)) == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sign-invalid-currency")))
                    return
                }
                event.setLine(0, GREEN.toString() + "[trader]")
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.trader-sign-valid")))
            } else {
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-trader-create")))
            }
        }
    }

}