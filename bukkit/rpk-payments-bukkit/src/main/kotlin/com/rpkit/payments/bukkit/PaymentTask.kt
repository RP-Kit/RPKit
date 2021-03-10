/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationImpl
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationService
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PaymentTask(private val plugin: RPKPaymentsBukkit) : BukkitRunnable() {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")

    override fun run() {
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        val bankService = Services[RPKBankService::class.java]
        val paymentNotificationService = Services[RPKPaymentNotificationService::class.java]
        if (paymentGroupService == null) return
        if (bankService == null) return
        if (paymentNotificationService == null) return
        paymentGroupService.paymentGroups
                .filter { group -> group.lastPaymentTime + group.interval < LocalDateTime.now() }
                .forEach { group ->
                    val currency = group.currency
                    if (currency != null) {
                        val now = LocalDateTime.now()
                        group.members.forEach { member ->
                            if (group.amount < 0) { // Character -> Payment Group, requires balance check on character
                                bankService.getBalance(member, currency).thenAccept { bankBalance ->
                                    if (bankBalance >= -group.amount) { // If character has enough money
                                        bankService.setBalance(member, currency, bankBalance + group.amount).thenRun {
                                            plugin.server.scheduler.runTask(plugin, Runnable {
                                                group.balance -= group.amount
                                                paymentGroupService.updatePaymentGroup(group)
                                            })
                                        }
                                    } else { // If character doesn't have enough money
                                        // Send notification to member
                                        val notificationMessage =
                                            plugin.messages["payment-notification-member-fail-to-pay", mapOf(
                                                "member" to member.name,
                                                "group" to group.name.value,
                                                "date" to dateFormat.format(now.atZone(ZoneId.systemDefault()))
                                            )]
                                        if (member.minecraftProfile?.isOnline == true) { // If online
                                            member.minecraftProfile?.sendMessage(notificationMessage)
                                        } else { // If offline
                                            paymentNotificationService.addPaymentNotification(
                                                RPKPaymentNotificationImpl(
                                                    group = group,
                                                    to = member,
                                                    character = member,
                                                    date = now,
                                                    text = notificationMessage
                                                )
                                            )
                                        }
                                        val ownerNotificationMessage =
                                            plugin.messages["payment-notification-owner-fail-to-pay", mapOf(
                                                "member" to member.name,
                                                "group" to group.name.value,
                                                "date" to dateFormat.format(now.atZone(ZoneId.systemDefault()))
                                            )]
                                        group.owners.forEach { owner ->
                                            if (owner.minecraftProfile?.isOnline != true) {
                                                paymentNotificationService.addPaymentNotification(
                                                    RPKPaymentNotificationImpl(
                                                        group = group,
                                                        to = owner,
                                                        character = member,
                                                        date = now,
                                                        text = ownerNotificationMessage
                                                    )
                                                )
                                            } else {
                                                owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
                                            }
                                        }
                                    }
                                }
                            } else if (group.amount > 0) { // Payment Group -> Character, requires balance check on group
                                if (group.balance >= group.amount) { // If group has enough money
                                    bankService.getBalance(member, currency).thenAccept { bankBalance ->
                                        bankService.setBalance(
                                            member,
                                            currency,
                                            bankBalance + group.amount
                                        ).thenRun {
                                            plugin.server.scheduler.runTask(plugin, Runnable {
                                                group.balance -= group.amount
                                                paymentGroupService.updatePaymentGroup(group)
                                            })
                                        }
                                    }
                                } else { // If group doesn't have enough money
                                    // Send notification to member
                                    val notificationMessage = plugin.messages["payment-notification-member-fail-to-be-paid", mapOf(
                                        "member" to member.name,
                                        "group" to group.name.value,
                                        "date" to dateFormat.format(now.atZone(ZoneId.systemDefault()))
                                    )]
                                    if (member.minecraftProfile?.isOnline != true) { // If offline
                                        paymentNotificationService.addPaymentNotification(
                                                RPKPaymentNotificationImpl(
                                                        group = group,
                                                        to = member,
                                                        character = member,
                                                        date = now,
                                                        text = notificationMessage
                                                )
                                        )
                                    } else { // If online
                                        member.minecraftProfile?.sendMessage(notificationMessage)
                                    }
                                    // Send notification to owners
                                    val ownerNotificationMessage = plugin.messages["payment-notification-owner-fail-to-be-paid", mapOf(
                                        "member" to member.name,
                                        "group" to group.name.value,
                                        "date" to dateFormat.format(now.atZone(ZoneId.systemDefault()))
                                    )]
                                    group.owners.forEach { owner ->
                                        if (owner.minecraftProfile?.isOnline != true) { // If offline
                                            paymentNotificationService.addPaymentNotification(
                                                    RPKPaymentNotificationImpl(
                                                            group = group,
                                                            to = owner,
                                                            character = member,
                                                            date = now,
                                                            text = ownerNotificationMessage
                                                    )
                                            )
                                        } else { // If online
                                            owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
                                        }
                                    }
                                }
                            }
                        }
                        // Update last payment time to avoid charging again in 1 minute
                        group.lastPaymentTime = LocalDateTime.now()
                        paymentGroupService.updatePaymentGroup(group)
                    }
                }
    }

}