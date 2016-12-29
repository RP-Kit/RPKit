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
import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.group.ElysiumPaymentGroupProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Payment list command.
 * Lists all payment groups currently involved in.
 */
class PaymentListCommand(private val plugin: ElysiumPaymentsBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("elysium.payments.command.payment.list")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
                val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPaymentGroupProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-list-title")))
                val paymentGroups = paymentGroupProvider.paymentGroups
                paymentGroups.filter { it.owners.contains(character) }
                        .forEach {
                            sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&',
                                        plugin.config.getString("messages.payment-list-item"))
                                        .replace("\$name", it.name)
                                        .replace("\$rank", "Owner")
                            )
                        }
                paymentGroups.filter { it.members.contains(character) }
                        .forEach {
                            sender.sendMessage(
                                    ChatColor.translateAlternateColorCodes('&',
                                            plugin.config.getString("messages.payment-list-item"))
                                            .replace("\$name", it.name)
                                            .replace("\$rank", "Member")
                            )
                        }
                paymentGroups.filter { it.invites.contains(character) }
                        .forEach {
                            sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&',
                                        plugin.config.getString("messages.payment-list-item"))
                                        .replace("\$name", it.name)
                                        .replace("\$rank", "Invited")
                            )
                        }
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-payment-list")))
        }
        return true
    }
}