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

package com.seventh_root.elysium.payments.bukkit.command.payment

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.group.ElysiumPaymentGroupImpl
import com.seventh_root.elysium.payments.bukkit.group.ElysiumPaymentGroupProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Payment create command.
 * Creates a payment group.
 */
class PaymentCreateCommand(private val plugin: ElysiumPaymentsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("elysium.payments.command.payment.create")) {
            if (args.isNotEmpty()) {
                val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPaymentGroupProvider::class)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
                val currencyName = plugin.config.getString("payment-groups.defaults.currency")
                val currency = if (currencyName == null)
                    currencyProvider.defaultCurrency
                else
                    currencyProvider.getCurrency(currencyName)
                val paymentGroup = ElysiumPaymentGroupImpl(
                        plugin,
                        name = args[0],
                        amount = plugin.config.getInt("payment-groups.defaults.amount"),
                        currency = currency,
                        interval = plugin.config.getLong("payment-groups.defaults.interval"),
                        lastPaymentTime = System.currentTimeMillis(),
                        balance = 0
                )
                paymentGroupProvider.addPaymentGroup(paymentGroup)
                if (sender is Player) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                    val player = playerProvider.getPlayer(sender)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        paymentGroup.addOwner(character)
                    }
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-create-valid")))
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-create-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-payment-create")))
        }
        return true
    }

}
