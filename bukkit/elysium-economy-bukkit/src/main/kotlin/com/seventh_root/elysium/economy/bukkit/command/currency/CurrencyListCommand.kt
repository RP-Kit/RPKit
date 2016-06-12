package com.seventh_root.elysium.economy.bukkit.command.currency

import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CurrencyListCommand(private val plugin: ElysiumEconomyBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("elysium.economy.command.currency.list")) {
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.currency-list-title")))
            for (currency in currencyProvider.currencies) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.currency-list-item"))
                        .replace("\$currency", currency.name))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-currency-list")))
        }
        return true
    }

}
