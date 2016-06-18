package com.seventh_root.elysium.banks.bukkit.listener

import com.seventh_root.elysium.banks.bukkit.ElysiumBanksBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent


class SignChangeListener(private val plugin: ElysiumBanksBukkit): Listener {
    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[bank]", ignoreCase = true)) {
            event.setLine(0, GREEN.toString() + "[bank]")
            if (!event.player.hasPermission("elysium.banks.sign.bank")) {
                event.block.breakNaturally()
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-bank-create")))
                return
            }
            if (!(event.getLine(1).equals("withdraw", ignoreCase = true) || event.getLine(1).equals("deposit", ignoreCase = true) || event.getLine(1).equals("balance", ignoreCase = true))) {
                event.block.breakNaturally()
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("bank-sign-invalid-operation")))
                return
            }
            if (event.getLine(1).equals("balance", ignoreCase = true)) {
                event.setLine(2, "")
            } else {
                event.setLine(2, "1")
            }
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java)
            if (currencyProvider.getCurrency(event.getLine(3)) == null) {
                val defaultCurrency = currencyProvider.defaultCurrency
                if (defaultCurrency == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-sign-invalid-currency")))
                    return
                } else {
                    event.setLine(3, defaultCurrency.name)
                }
            }
        }
    }
}