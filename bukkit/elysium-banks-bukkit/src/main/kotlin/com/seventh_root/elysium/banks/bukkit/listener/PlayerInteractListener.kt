package com.seventh_root.elysium.banks.bukkit.listener

import com.seventh_root.elysium.banks.bukkit.ElysiumBanksBukkit
import com.seventh_root.elysium.banks.bukkit.bank.ElysiumBankProvider
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: ElysiumBanksBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasBlock()) {
            if (event.clickedBlock.state is Sign) {
                val sign = event.clickedBlock.state as Sign
                if (sign.getLine(0).equals(GREEN.toString() + "[bank]", ignoreCase = true)) {
                    event.isCancelled = true
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java)
                    val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
                    val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class.java)
                    val player = playerProvider.getPlayer(event.player)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        val currency = currencyProvider.getCurrency(sign.getLine(3))
                        if (currency != null) {
                            if (event.action == RIGHT_CLICK_BLOCK) {
                                when (sign.getLine(2)) {
                                    "1" -> {
                                        sign.setLine(2, "10")
                                        sign.update()
                                    }
                                    "10" -> {
                                        sign.setLine(2, "100")
                                        sign.update()
                                    }
                                    "100" -> {
                                        sign.setLine(2, "1000")
                                        sign.update()
                                    }
                                    "1000" -> {
                                        sign.setLine(2, "1")
                                        sign.update()
                                    }
                                    else -> {
                                    }
                                }
                            } else if (event.action == Action.LEFT_CLICK_BLOCK) {
                                if (sign.getLine(1).equals("withdraw", ignoreCase = true)) {
                                    if (economyProvider.getBalance(character, currency) + sign.getLine(2).toInt() > 1728) {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-withdraw-invalid-wallet-full")))
                                    } else if (sign.getLine(2).toInt() > bankProvider.getBalance(character, currency)) {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-withdraw-invalid-not-enough-money")))
                                    } else {
                                        bankProvider.setBalance(character, currency, bankProvider.getBalance(character, currency) - sign.getLine(2).toInt())
                                        economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + sign.getLine(2).toInt())
                                        event.player.sendMessage(
                                                ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-withdraw-valid"))
                                                        .replace("\$amount", sign.getLine(2))
                                                        .replace("\$currency", if (sign.getLine(2).toInt() == 1) currency.nameSingular else currency.namePlural)
                                                        .replace("\$wallet-balance", economyProvider.getBalance(character, currency).toString())
                                                        .replace("\$bank-balance", bankProvider.getBalance(character, currency).toString()))
                                    }
                                } else if (sign.getLine(1).equals("deposit", ignoreCase = true)) {
                                    if (sign.getLine(2).toInt() > economyProvider.getBalance(character, currency)) {
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-deposit-invalid-not-enough-money")))
                                    } else {
                                        bankProvider.setBalance(character, currency, bankProvider.getBalance(character, currency) + sign.getLine(2).toInt())
                                        economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - sign.getLine(2).toInt())
                                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-deposit-valid"))
                                                .replace("\$amount", sign.getLine(2))
                                                .replace("\$currency", if (sign.getLine(2).toInt() == 1) currency.nameSingular else currency.namePlural)
                                                .replace("\$wallet-balance", economyProvider.getBalance(character, currency).toString())
                                                .replace("\$bank-balance", bankProvider.getBalance(character, currency).toString()))
                                    }
                                } else if (sign.getLine(1).equals("balance", ignoreCase = true)) {
                                    val balance = bankProvider.getBalance(character, currency)
                                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.bank-balance-valid"))
                                            .replace("\$amount", balance.toString())
                                            .replace("\$currency", if (balance == 1) currency.nameSingular else currency.namePlural))

                                }
                            }
                        }
                    }
                }
            }
        }
    }

}