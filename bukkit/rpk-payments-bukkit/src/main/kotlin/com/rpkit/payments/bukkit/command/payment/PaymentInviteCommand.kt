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
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat
import java.util.*

/**
 * Payment invite command.
 * Invites a character to a payment group.
 */
class PaymentInviteCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.payments.command.payment.invite")) {
            if (args.size > 1) {
                val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                val paymentGroup = paymentGroupProvider.getPaymentGroup(args.dropLast(1).joinToString(" "))
                if (paymentGroup != null) {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val bukkitPlayer = plugin.server.getOfflinePlayer(args.last())
                    val player = playerProvider.getPlayer(bukkitPlayer)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        paymentGroup.addInvite(character)
                        sender.sendMessage(plugin.core.messages["payment-invite-valid"])
                        val paymentNotificationProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentNotificationProvider::class)
                        val now = System.currentTimeMillis()
                        val notificationMessage = plugin.core.messages["payment-notification-invite", mapOf(
                                Pair("member", character.name),
                                Pair("group", paymentGroup.name),
                                Pair("date", dateFormat.format(Date(now)))
                        )]
                        if (!(character.player?.bukkitPlayer?.isOnline?:false)) { // If offline
                            paymentNotificationProvider.addPaymentNotification(
                                    RPKPaymentNotificationImpl(
                                            group = paymentGroup,
                                            to = character,
                                            character = character,
                                            date = now,
                                            text = notificationMessage
                                    )
                            )
                        } else { // If online
                            character.player?.bukkitPlayer?.player?.sendMessage(notificationMessage)
                        }
                    } else {
                        sender.sendMessage(plugin.core.messages["payment-invite-invalid-character"])
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["payment-invite-invalid-group"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["payment-invite-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-payment-invite"])
        }
        return true
    }


}