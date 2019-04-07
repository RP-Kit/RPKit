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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

/**
 * Payment leave command.
 * Leaves a payment group.
 */
class PaymentLeaveCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.payments.command.payment.leave")) {
            if (args.isNotEmpty()) {
                if (sender is Player) {
                    val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                    val paymentGroup = paymentGroupProvider.getPaymentGroup(args.joinToString(" "))
                    if (paymentGroup != null) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                        if (minecraftProfile != null) {
                            val character = characterProvider.getActiveCharacter(minecraftProfile)
                            if (character != null) {
                                if (paymentGroup.members.contains(character)) {
                                    paymentGroup.removeMember(character)
                                    sender.sendMessage(plugin.messages["payment-leave-valid"])
                                    val paymentNotificationProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentNotificationProvider::class)
                                    val now = System.currentTimeMillis()
                                    val ownerNotificationMessage = plugin.messages["payment-notification-member-leave", mapOf(
                                            Pair("member", character.name),
                                            Pair("group", paymentGroup.name),
                                            Pair("date", dateFormat.format(Date(now)))
                                    )]
                                    paymentGroup.owners.forEach { owner ->
                                        if (owner.minecraftProfile?.isOnline != true) {
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
                                            owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
                                        }
                                    }
                                } else {
                                    sender.sendMessage(plugin.messages["payment-leave-invalid-member"])
                                }
                            } else {
                                sender.sendMessage(plugin.messages["payment-leave-invalid-character"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-minecraft-profile"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["payment-leave-invalid-group"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["not-from-console"])
                }
            } else {
                sender.sendMessage(plugin.messages["payment-leave-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-payment-leave"])
        }
        return true
    }
}