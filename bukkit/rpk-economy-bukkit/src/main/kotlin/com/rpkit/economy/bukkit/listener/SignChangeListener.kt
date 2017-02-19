package com.rpkit.economy.bukkit.listener

import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import org.bukkit.ChatColor.GREEN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent


class SignChangeListener(private val plugin: RPKEconomyBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[exchange]", ignoreCase = true)) {
            event.setLine(0, GREEN.toString() + "[exchange]")
            if (!event.player.hasPermission("rpkit.economy.sign.exchange")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.core.messages["no-permission-exchange-create"])
                return
            }
            if (!event.getLine(1).matches(Regex("\\d+\\s+.*"))) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.core.messages["exchange-sign-invalid-format-from"])
                return
            }
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val fromCurrencyName = event.getLine(1).replaceFirst(Regex("\\d+\\s+"), "")
            val fromCurrency = currencyProvider.getCurrency(fromCurrencyName)
            if (fromCurrency == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.core.messages["exchange-sign-invalid-currency-from"])
                return
            }
            event.setLine(2, "for")
            val toCurrencyName = event.getLine(3)
            val toCurrency = currencyProvider.getCurrency(toCurrencyName)
            if (toCurrency == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.core.messages["exchange-sign-invalid-currency-to"])
                return
            }
        }
    }

}