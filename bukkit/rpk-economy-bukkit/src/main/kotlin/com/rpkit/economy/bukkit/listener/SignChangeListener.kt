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
        if (event.getLine(0).equals("[exchange]", ignoreCase = true)) { // Exchange signs
            event.setLine(0, "$GREEN[exchange]")
            if (!event.player.hasPermission("rpkit.economy.sign.exchange")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-permission-exchange-create"])
                return
            }
            if (event.getLine(1)?.matches(Regex("\\d+\\s+.*")) != true) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["exchange-sign-invalid-format-from"])
                return
            }
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val fromCurrencyName = event.getLine(1)?.replaceFirst(Regex("\\d+\\s+"), "") ?: ""
            val fromCurrency = currencyProvider.getCurrency(fromCurrencyName)
            if (fromCurrency == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["exchange-sign-invalid-currency-from"])
                return
            }
            event.setLine(2, "for")
            val toCurrencyName = event.getLine(3) ?: ""
            val toCurrency = currencyProvider.getCurrency(toCurrencyName)
            if (toCurrency == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["exchange-sign-invalid-currency-to"])
                return
            }
        } else if (event.getLine(0).equals("[dynexchange]", ignoreCase = true)) { // Dynamic exchange signs
            event.setLine(0, "$GREEN[dynexchange]")
            if (!event.player.hasPermission("rpkit.economy.sign.dynexchange")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-permission-dynexchange-create"])
                return
            }
            if (event.getLine(1)?.matches(Regex("\\d+\\s+.*")) != true) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["dynexchange-sign-invalid-format-from"])
                return
            }
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val fromCurrencyName = event.getLine(1)?.replaceFirst(Regex("\\d+\\s+"), "") ?: ""
            val fromCurrency = currencyProvider.getCurrency(fromCurrencyName)
            if (fromCurrency == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["dynexchange-sign-invalid-currency-from"])
                return
            }
            event.setLine(2, "for")
            if (event.getLine(3)?.matches(Regex("\\d+\\s+.+")) != true) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["dynexchange-sign-invalid-format-to"])
                return
            }
            val toCurrencyName = event.getLine(3) ?: ""
            val toCurrency = currencyProvider.getCurrency(toCurrencyName)
            if (toCurrency == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["dynexchange-sign-invalid-currency-to"])
                return
            }
        }
    }

}