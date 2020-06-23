/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Payment list command.
 * Lists all payment groups currently involved in.
 */
class PaymentListCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.payments.command.payment.list")) {
            if (sender is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    sender.sendMessage(plugin.messages["payment-list-title"])
                    val paymentGroups = paymentGroupProvider.paymentGroups
                    paymentGroups.filter { it.owners.contains(character) }
                            .forEach {
                                sender.sendMessage(
                                        plugin.messages["payment-list-item", mapOf(
                                                Pair("name", it.name),
                                                Pair("rank", "Owner")
                                        )]
                                )
                            }
                    paymentGroups.filter { it.members.contains(character) }
                            .forEach {
                                sender.sendMessage(
                                        plugin.messages[".payment-list-item", mapOf(
                                                Pair("name", it.name),
                                                Pair("rank", "Member")
                                        )]
                                )
                            }
                    paymentGroups.filter { it.invites.contains(character) }
                            .forEach {
                                sender.sendMessage(
                                        plugin.messages["payment-list-item", mapOf(
                                                Pair("name", it.name),
                                                Pair("rank", "Invited")
                                        )]
                                )
                            }
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-payment-list"])
        }
        return true
    }
}