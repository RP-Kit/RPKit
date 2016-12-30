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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProvider
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationImpl
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

/**
 * Payment join command.
 * Joins a payment group.
 */
class PaymentJoinCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.payments.command.payment.join")) {
            if (args.isNotEmpty()) {
                if (sender is Player) {
                    val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                    val paymentGroup = paymentGroupProvider.getPaymentGroup(args[0])
                    if (paymentGroup != null) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val player = playerProvider.getPlayer(sender)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            if (paymentGroup.invites.contains(character)) {
                                paymentGroup.removeInvite(character)
                                paymentGroup.addMember(character)
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-join-valid")))
                                val paymentNotificationProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentNotificationProvider::class)
                                val now = System.currentTimeMillis()
                                val ownerNotificationMessage = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-notification-member-join"))
                                        .replace("\$member", character.name)
                                        .replace("\$group", paymentGroup.name)
                                        .replace("\$date", dateFormat.format(Date(now)))
                                paymentGroup.owners.forEach { owner ->
                                    if (!(owner.player?.bukkitPlayer?.isOnline?:false)) {
                                        paymentNotificationProvider.addPaymentNotification(
                                                RPKPaymentNotificationImpl(
                                                        group = paymentGroup,
                                                        to = owner,
                                                        character = character,
                                                        date = now,
                                                        text = ownerNotificationMessage
                                                )
                                        )
                                    } else {
                                        owner.player?.bukkitPlayer?.player?.sendMessage(ownerNotificationMessage)
                                    }
                                }
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-join-invalid-invite")))
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-join-invalid-character")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-join-invalid-group")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.payment-join-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-payment-join")))
        }
        return true
    }
}