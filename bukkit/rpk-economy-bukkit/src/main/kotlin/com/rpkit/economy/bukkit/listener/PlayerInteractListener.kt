package com.rpkit.economy.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: RPKEconomyBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == RIGHT_CLICK_BLOCK) {
            val sign = event.clickedBlock.state
            if (sign is Sign) {
                if (sign.getLine(0).equals(ChatColor.GREEN.toString() + "[exchange]", ignoreCase = true)) { // Exchange signs
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                    val amount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
                    val fromCurrency = currencyProvider.getCurrency(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), ""))
                    val toCurrency = currencyProvider.getCurrency(sign.getLine(3))
                    if (fromCurrency != null && toCurrency != null) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
                        if (minecraftProfile != null) {
                            val character = characterProvider.getActiveCharacter(minecraftProfile)
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
                            event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                        }
                    } else {
                        event.player.sendMessage(plugin.messages["exchange-invalid-format"])
                    }
                } else if (sign.getLine(0).equals(ChatColor.GREEN.toString() + "[dynexchange]", ignoreCase = true)) { // Dynamic exchange signs
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                    val fromAmount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
                    val fromCurrency = currencyProvider.getCurrency(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), ""))
                    val toAmount = sign.getLine(3).split(Regex("\\s+"))[0].toInt()
                    val toCurrency = currencyProvider.getCurrency(sign.getLine(3).replaceFirst(Regex("\\d+\\s+"), ""))
                    if (fromCurrency != null && toCurrency != null) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
                        if (minecraftProfile != null) {
                            val character = characterProvider.getActiveCharacter(minecraftProfile)
                            if (character != null) {
                                if (economyProvider.getBalance(character, fromCurrency) - fromAmount >= 0) {
                                    if (economyProvider.getBalance(character, toCurrency) + toAmount <= 1728) {
                                        economyProvider.setBalance(character, fromCurrency, economyProvider.getBalance(character, fromCurrency) - fromAmount)
                                        economyProvider.setBalance(character, toCurrency, economyProvider.getBalance(character, toCurrency) + toAmount)
                                        var rate = fromAmount.toDouble() / toAmount.toDouble()
                                        val rateChange = plugin.config.getDouble("dynamic-exchanges.rate-change")
                                        rate -= rateChange
                                        val newToAmount = Math.round(fromAmount.toDouble() / rate)
                                        sign.setLine(3, "$newToAmount ${toCurrency.name}")
                                        sign.update()
                                        event.player.sendMessage(plugin.messages["exchange-valid", mapOf(
                                                Pair("from-amount", fromAmount.toString()),
                                                Pair("to-amount", toAmount.toString()),
                                                Pair("from-currency", if (fromAmount == 1) fromCurrency.nameSingular else fromCurrency.namePlural),
                                                Pair("to-currency", if (toAmount == 1) toCurrency.nameSingular else toCurrency.namePlural)
                                        )])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (event.action == LEFT_CLICK_BLOCK) {
            val sign = event.clickedBlock.state
            if (sign is Sign) {
                if (sign.getLine(0).equals(ChatColor.GREEN.toString() + "[dynexchange]", ignoreCase = true)) { // Dynamic exchange signs
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                    val fromAmount = sign.getLine(3).split(Regex("\\s+"))[0].toInt()
                    val fromCurrency = currencyProvider.getCurrency(sign.getLine(3).replaceFirst(Regex("\\d+\\s+"), ""))
                    val toAmount = sign.getLine(1).split(Regex("\\s+"))[0].toInt()
                    val toCurrency = currencyProvider.getCurrency(sign.getLine(1).replaceFirst(Regex("\\d+\\s+"), ""))
                    if (fromCurrency != null && toCurrency != null) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
                        if (minecraftProfile != null) {
                            val character = characterProvider.getActiveCharacter(minecraftProfile)
                            if (character != null) {
                                if (economyProvider.getBalance(character, fromCurrency) - fromAmount >= 0) {
                                    if (economyProvider.getBalance(character, toCurrency) + toAmount <= 1728) {
                                        economyProvider.setBalance(character, fromCurrency, economyProvider.getBalance(character, fromCurrency) - fromAmount)
                                        economyProvider.setBalance(character, toCurrency, economyProvider.getBalance(character, toCurrency) + toAmount)
                                        var rate = fromAmount.toDouble() / toAmount.toDouble()
                                        val rateChange = plugin.config.getDouble("dynamic-exchanges.rate-change")
                                        rate += rateChange
                                        val newToAmount = Math.round(fromAmount.toDouble() / rate)
                                        sign.setLine(3, "$newToAmount ${toCurrency.name}")
                                        sign.update()
                                        event.player.sendMessage(plugin.messages["exchange-valid", mapOf(
                                                Pair("from-amount", fromAmount.toString()),
                                                Pair("to-amount", toAmount.toString()),
                                                Pair("from-currency", if (fromAmount == 1) fromCurrency.nameSingular else fromCurrency.namePlural),
                                                Pair("to-currency", if (toAmount == 1) toCurrency.nameSingular else toCurrency.namePlural)
                                        )])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}