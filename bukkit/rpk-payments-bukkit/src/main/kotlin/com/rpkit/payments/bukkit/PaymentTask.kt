/*
 * Copyright 2022 Ren Binden
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

import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PaymentTask(private val plugin: RPKPaymentsBukkit) : BukkitRunnable() {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")

    override fun run() {
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        val bankService = Services[RPKBankService::class.java]
        val notificationService = Services[RPKNotificationService::class.java]
        if (paymentGroupService == null) return
        if (bankService == null) return
        if (notificationService == null) return
        paymentGroupService.paymentGroups.thenAccept { paymentGroups ->
            paymentGroups.filter { group -> group.lastPaymentTime + group.interval < LocalDateTime.now() }
                .forEach { group ->
                    val currency = group.currency
                    if (currency != null) {
                        val now = LocalDateTime.now()
                        group.members.thenAccept { members ->
                            members.forEach { member ->
                                if (group.amount < 0) { // Character -> Payment Group, requires balance check on character
                                    bankService.getBalance(member, currency).thenAccept { bankBalance ->
                                        if (bankBalance >= -group.amount) { // If character has enough money
                                            bankService.setBalance(member, currency, bankBalance + group.amount)
                                                .thenRun {
                                                    group.balance -= group.amount
                                                    paymentGroupService.updatePaymentGroup(group)
                                                }
                                        } else { // If character doesn't have enough money
                                            // Send notification to member
                                            val notificationTitle = plugin.messages.paymentNotificationMemberFailToPayTitle.withParameters(
                                                member = member,
                                                group = group,
                                                date = now.atZone(ZoneId.systemDefault())
                                            )
                                            val notificationMessage = plugin.messages.paymentNotificationMemberFailToPay.withParameters(
                                                member = member,
                                                group = group,
                                                date = now.atZone(ZoneId.systemDefault())
                                            )
                                            if (member.minecraftProfile?.isOnline == true) { // If online
                                                member.minecraftProfile?.sendMessage(notificationMessage)
                                            } else { // If offline
                                                val profile = member.profile
                                                if (profile != null) {
                                                    notificationService.createNotification(
                                                        profile,
                                                        notificationTitle,
                                                        notificationMessage
                                                    )
                                                }
                                            }
                                            val ownerNotificationTitle = plugin.messages.paymentNotificationOwnerFailToPayTitle
                                                .withParameters(
                                                    member = member,
                                                    group = group,
                                                    date = now.atZone(ZoneId.systemDefault())
                                                )
                                            val ownerNotificationMessage = plugin.messages.paymentNotificationOwnerFailToPay
                                                .withParameters(
                                                    member = member,
                                                    group = group,
                                                    date = now.atZone(ZoneId.systemDefault())
                                                )
                                            group.owners.thenAccept { owners ->
                                                owners.forEach { owner ->
                                                    if (owner.minecraftProfile?.isOnline != true) {
                                                        val profile = owner.profile
                                                        if (profile != null) {
                                                            notificationService.createNotification(
                                                                profile,
                                                                ownerNotificationTitle,
                                                                ownerNotificationMessage
                                                            )
                                                        }
                                                    } else {
                                                        owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
                                                    }
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
                                                group.balance -= group.amount
                                                paymentGroupService.updatePaymentGroup(group)
                                            }
                                        }
                                    } else { // If group doesn't have enough money
                                        // Send notification to member
                                        val notificationTitle = plugin.messages.paymentNotificationMemberFailToBePaidTitle
                                            .withParameters(
                                                member = member,
                                                group = group,
                                                date = now.atZone(ZoneId.systemDefault())
                                            )
                                        val notificationMessage = plugin.messages.paymentNotificationMemberFailToBePaid
                                            .withParameters(
                                                member = member,
                                                group = group,
                                                date = now.atZone(ZoneId.systemDefault())
                                            )
                                        if (member.minecraftProfile?.isOnline != true) { // If offline
                                            val profile = member.profile
                                            if (profile != null) {
                                                notificationService.createNotification(
                                                    profile,
                                                    notificationTitle,
                                                    notificationMessage
                                                )
                                            }
                                        } else { // If online
                                            member.minecraftProfile?.sendMessage(notificationMessage)
                                        }
                                        // Send notification to owners
                                        val ownerNotificationTitle = plugin.messages.paymentNotificationOwnerFailToPayTitle
                                            .withParameters(
                                                member = member,
                                                group = group,
                                                now.atZone(ZoneId.systemDefault())
                                            )
                                        val ownerNotificationMessage =
                                            plugin.messages.paymentNotificationOwnerFailToPay
                                                .withParameters(
                                                    member = member,
                                                    group = group,
                                                    date = now.atZone(ZoneId.systemDefault())
                                                )
                                        group.owners.thenAccept { owners ->
                                            owners.forEach { owner ->
                                                if (owner.minecraftProfile?.isOnline != true) { // If offline
                                                    val ownerProfile = owner.profile
                                                    if (ownerProfile != null) {
                                                        notificationService.createNotification(
                                                            ownerProfile,
                                                            ownerNotificationTitle,
                                                            ownerNotificationMessage
                                                        )
                                                    }
                                                } else { // If online
                                                    owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
                                                }
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
    }

}