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

package com.rpkit.payments.bukkit

import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.payments.bukkit.command.payment.PaymentCommand
import com.rpkit.payments.bukkit.database.table.*
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProvider
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProviderImpl
import com.rpkit.payments.bukkit.listener.PlayerJoinListener
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationImpl
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationProvider
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationProviderImpl
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable
import java.text.SimpleDateFormat
import java.util.*

/**
 * RPK payments plugin default implementation.
 */
class RPKPaymentsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKPaymentGroupProviderImpl(this),
                RPKPaymentNotificationProviderImpl(this)
        )
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")
        object: BukkitRunnable() {
            override fun run() {
                val paymentGroupProvider = core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
                val bankProvider = core.serviceManager.getServiceProvider(RPKBankProvider::class)
                val paymentNotificationProvider = core.serviceManager.getServiceProvider(RPKPaymentNotificationProvider::class)
                paymentGroupProvider.paymentGroups
                        .filter { group -> group.lastPaymentTime + group.interval < System.currentTimeMillis() }
                        .forEach { group ->
                            val currency = group.currency
                            if (currency != null) {
                                val now = System.currentTimeMillis()
                                group.members.forEach { member ->
                                    if (group.amount < 0) { // Character -> Payment Group, requires balance check on character
                                        if (bankProvider.getBalance(member, currency) >= -group.amount) { // If character has enough money
                                            bankProvider.setBalance(member, currency, bankProvider.getBalance(member, currency) + group.amount)
                                            group.balance -= group.amount
                                            paymentGroupProvider.updatePaymentGroup(group)
                                        } else { // If character doesn't have enough money
                                            // Send notification to member
                                            val notificationMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.payment-notification-member-fail-to-pay"))
                                                    .replace("\$member", member.name)
                                                    .replace("\$group", group.name)
                                                    .replace("\$date", dateFormat.format(Date(now)))
                                            if (!(member.player?.bukkitPlayer?.isOnline?:false)) { // If offline
                                                paymentNotificationProvider.addPaymentNotification(
                                                        RPKPaymentNotificationImpl(
                                                                group = group,
                                                                to = member,
                                                                character = member,
                                                                date = now,
                                                                text = notificationMessage
                                                        )
                                                )
                                            } else { // If online
                                                member.player?.bukkitPlayer?.player?.sendMessage(notificationMessage)
                                            }
                                            val ownerNotificationMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.payment-notification-owner-fail-to-pay"))
                                                    .replace("\$member", member.name)
                                                    .replace("\$group", group.name)
                                                    .replace("\$date", dateFormat.format(Date(now)))
                                            group.owners.forEach { owner ->
                                                if (!(owner.player?.bukkitPlayer?.isOnline?:false)) {
                                                    paymentNotificationProvider.addPaymentNotification(
                                                            RPKPaymentNotificationImpl(
                                                                    group = group,
                                                                    to = owner,
                                                                    character = member,
                                                                    date = now,
                                                                    text = ownerNotificationMessage
                                                            )
                                                    )
                                                } else {
                                                    owner.player?.bukkitPlayer?.player?.sendMessage(ownerNotificationMessage)
                                                }
                                            }
                                        }
                                    } else if (group.amount > 0) { // Payment Group -> Character, requires balance check on group
                                        if (group.balance >= group.amount) { // If group has enough money
                                            bankProvider.setBalance(member, currency, bankProvider.getBalance(member, currency) + group.amount)
                                            group.balance -= group.amount
                                            paymentGroupProvider.updatePaymentGroup(group)
                                        } else { // If group doesn't have enough money
                                            // Send notification to member
                                            val notificationMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.payment-notification-member-fail-to-be-paid"))
                                                    .replace("\$member", member.name)
                                                    .replace("\$group", group.name)
                                                    .replace("\$date", dateFormat.format(Date(now)))
                                            if (!(member.player?.bukkitPlayer?.isOnline?:false)) { // If offline
                                                paymentNotificationProvider.addPaymentNotification(
                                                        RPKPaymentNotificationImpl(
                                                                group = group,
                                                                to = member,
                                                                character = member,
                                                                date = now,
                                                                text = notificationMessage
                                                        )
                                                )
                                            } else { // If online
                                                member.player?.bukkitPlayer?.player?.sendMessage(notificationMessage)
                                            }
                                            // Send notification to owners
                                            val ownerNotificationMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.payment-notification-owner-fail-to-be-paid"))
                                                    .replace("\$member", member.name)
                                                    .replace("\$group", group.name)
                                                    .replace("\$date", dateFormat.format(Date(now)))
                                            group.owners.forEach { owner ->
                                                if (!(owner.player?.bukkitPlayer?.isOnline?:false)) { // If offline
                                                    paymentNotificationProvider.addPaymentNotification(
                                                            RPKPaymentNotificationImpl(
                                                                    group = group,
                                                                    to = owner,
                                                                    character = member,
                                                                    date = now,
                                                                    text = ownerNotificationMessage
                                                            )
                                                    )
                                                } else { // If online
                                                    owner.player?.bukkitPlayer?.player?.sendMessage(ownerNotificationMessage)
                                                }
                                            }
                                        }
                                    }
                                }
                                // Update last payment time to avoid charging again in 1 minute
                                group.lastPaymentTime = System.currentTimeMillis()
                                paymentGroupProvider.updatePaymentGroup(group)
                            }
                        }
            }
        }.runTaskTimer(this, 1200L, 1200L) // Keep payments accurate to 1 minute (60 seconds * 20 ticks)
    }

    override fun registerCommands() {
        getCommand("payment").executor = PaymentCommand(this)
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKPaymentGroupTable(database, this))
        database.addTable(RPKPaymentGroupInviteTable(database, this))
        database.addTable(RPKPaymentGroupMemberTable(database, this))
        database.addTable(RPKPaymentGroupOwnerTable(database, this))
        database.addTable(RPKPaymentNotificationTable(database, this))
    }
}
