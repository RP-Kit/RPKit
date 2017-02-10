/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.payments.bukkit.command.payment

import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Payment deposit command.
 * Deposits money into a payment group.
 */
class PaymentDepositCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.payments.command.payment.deposit")) {
            if (sender is Player) {
                if (args.size > 1) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                    val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
                    val player = playerProvider.getPlayer(sender)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        val paymentGroup = paymentGroupProvider.getPaymentGroup(args.dropLast(1).joinToString(" "))
                        if (paymentGroup != null) {
                            if (paymentGroup.owners.contains(character)) {
                                val currency = paymentGroup.currency
                                if (currency != null) {
                                    try {
                                        val amount = args.last().toInt()
                                        if (amount > 0) {
                                            if (bankProvider.getBalance(character, currency) >= amount) {
                                                bankProvider.setBalance(character, currency, bankProvider.getBalance(character, currency) - amount)
                                                paymentGroup.balance = paymentGroup.balance + amount
                                                paymentGroupProvider.updatePaymentGroup(paymentGroup)
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-valid")))
                                            } else {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-balance")))
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-amount")))
                                        }
                                    } catch (exception: NumberFormatException) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-amount")))
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-currency")))
                                }
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-owner")))
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-group")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-invalid-character")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-deposit-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-payment-deposit")))
        }
        return true
    }
}