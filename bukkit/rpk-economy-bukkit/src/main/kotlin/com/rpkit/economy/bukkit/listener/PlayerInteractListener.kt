package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: RPKEconomyBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == RIGHT_CLICK_BLOCK) {
            val sign = event.clickedBlock.state
            if (sign is Sign) {
                if (sign.getLine(0).equals(ChatColor.GREEN.toString() + "[exchange]", ignoreCase = true)) {
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                    val amount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
                    val fromCurrency = currencyProvider.getCurrency(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), ""))
                    val toCurrency = currencyProvider.getCurrency(sign.getLine(3))
                    if (fromCurrency != null && toCurrency != null) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                        val player = playerProvider.getPlayer(event.player)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            if (economyProvider.getBalance(character, fromCurrency) - amount >= 0) {
                                val convertedAmount = fromCurrency.convert(amount, toCurrency)
                                if (economyProvider.getBalance(character, toCurrency) + convertedAmount <= 1728) {
                                    economyProvider.setBalance(character, fromCurrency, economyProvider.getBalance(character, fromCurrency) - amount)
                                    economyProvider.setBalance(character, toCurrency, economyProvider.getBalance(character, toCurrency) + convertedAmount)
                                    event.player.sendMessage(plugin.messages["exchange-valid", mapOf(
                                            Pair("from-amount", amount.toString()),
                                            Pair("to-amount", convertedAmount.toString()),
                                            Pair("from-currency", if (amount == 1) fromCurrency.nameSingular else fromCurrency.namePlural),
                                            Pair("to-currency", if (convertedAmount == 1) toCurrency.nameSingular else toCurrency.namePlural)
                                    )])
                                } else {
                                    event.player.sendMessage(plugin.messages["exchange-invalid-wallet-balance-too-high"])
                                }
                            } else {
                                event.player.sendMessage(plugin.messages["exchange-invalid-wallet-balance-too-low"])
                            }
                        } else {
                            event.player.sendMessage(plugin.messages["no-character"])
                        }
                    } else {
                        event.player.sendMessage(plugin.messages["exchange-invalid-format"])
                    }
                }
            }
        }
    }

}