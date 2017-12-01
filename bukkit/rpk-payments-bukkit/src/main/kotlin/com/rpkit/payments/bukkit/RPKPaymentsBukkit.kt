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
                                            val notificationMessage = messages["payment-notification-member-fail-to-pay", mapOf(
                                                    Pair("member", member.name),
                                                    Pair("group", group.name),
                                                    Pair("date", dateFormat.format(Date(now)))
                                            )]
                                            if (!(member.minecraftProfile?.isOnline?:false)) { // If offline
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
                                                member.minecraftProfile?.sendMessage(notificationMessage)
                                            }
                                            val ownerNotificationMessage = messages["payment-notification-owner-fail-to-pay", mapOf(
                                                    Pair("member", member.name),
                                                    Pair("group", group.name),
                                                    Pair("date", dateFormat.format(Date(now)))
                                            )]
                                            group.owners.forEach { owner ->
                                                if (!(owner.minecraftProfile?.isOnline?:false)) {
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
                                                    owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
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
                                            val notificationMessage = messages["payment-notification-member-fail-to-be-paid", mapOf(
                                                    Pair("member", member.name),
                                                    Pair("group", group.name),
                                                    Pair("date", dateFormat.format(Date(now)))
                                            )]
                                            if (!(member.minecraftProfile?.isOnline?:false)) { // If offline
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
                                                member.minecraftProfile?.sendMessage(notificationMessage)
                                            }
                                            // Send notification to owners
                                            val ownerNotificationMessage = messages["payment-notification-owner-fail-to-be-paid", mapOf(
                                                    Pair("member", member.name),
                                                    Pair("group", group.name),
                                                    Pair("date", dateFormat.format(Date(now)))
                                            )]
                                            group.owners.forEach { owner ->
                                                if (!(owner.minecraftProfile?.isOnline?:false)) { // If offline
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
                                                    owner.minecraftProfile?.sendMessage(ownerNotificationMessage)
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

    override fun setDefaultMessages() {
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("operation-cancelled", "&cOperation cancelled.")
        messages.setDefault("payment-usage", "&cUsage: /payment [create|invite|kick|join|leave|withdraw|deposit|list|info|set]")
        messages.setDefault("payment-create-valid", "&aPayment group created.")
        messages.setDefault("payment-create-invalid-name-already-exists", "&cA payment group by that name already exists.")
        messages.setDefault("payment-create-usage", "&cUsage: /payment create [group]")
        messages.setDefault("payment-invite-valid", "&aInvitation sent to character.")
        messages.setDefault("payment-invite-invalid-character", "&cThat player does not have an active character.")
        messages.setDefault("payment-invite-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-invite-usage", "&cUsage: /payment invite [group] [player]")
        messages.setDefault("payment-kick-valid", "&aCharacter kicked from payment group.")
        messages.setDefault("payment-kick-invalid-character", "&cThat player does not have an active character.")
        messages.setDefault("payment-kick-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-kick-usage", "&cUsage: /payment kick [group] [player]")
        messages.setDefault("payment-join-valid", "&aJoined payment group.")
        messages.setDefault("payment-join-invalid-invite", "&cYou must have an invite to join a payment group.")
        messages.setDefault("payment-join-invalid-character", "&cYou must have a character in order to join a payment group.")
        messages.setDefault("payment-join-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-join-usage", "&cUsage: /payment join [group]")
        messages.setDefault("payment-leave-valid", "&aLeft payment group.")
        messages.setDefault("payment-leave-invalid-member", "&cYou are not a member of that payment group.")
        messages.setDefault("payment-leave-invalid-character", "&cYou must have a character in order to leave a payment group.")
        messages.setDefault("payment-leave-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-leave-usage", "&cUsage: /payment leave [group]")
        messages.setDefault("payment-withdraw-valid", "&aWithdrew money from the payment group.")
        messages.setDefault("payment-withdraw-invalid-balance", "&cThe payment group does not have enough money to withdraw that amount.")
        messages.setDefault("payment-withdraw-invalid-amount", "&cYou must specify a positive amount to withdraw. If you wish to deposit instead, use /payment deposit.")
        messages.setDefault("payment-withdraw-invalid-currency", "&cThat payment group doesn't yet have a currency set, so it is impossible to withdraw money.")
        messages.setDefault("payment-withdraw-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-withdraw-invalid-character", "&cYou must have an active character in order to withdraw money from a payment group.")
        messages.setDefault("payment-withdraw-invalid-owner", "&cYou must be an owner of the payment group in order to withdraw money.")
        messages.setDefault("payment-withdraw-usage", "&cUsage: /payment withdraw [group] [amount]")
        messages.setDefault("payment-deposit-valid", "&aDeposited money to the payment group.")
        messages.setDefault("payment-deposit-invalid-balance", "&cYou do not have enough money in your bank account to deposit that amount.")
        messages.setDefault("payment-deposit-invalid-amount", "&cYou must specify a positive amount to deposit. If you wish to withdraw instead, use /payment withdraw.")
        messages.setDefault("payment-deposit-invalid-currency", "&cThat payment group doesn't yet have a currency set, so it is impossible to deposit money.")
        messages.setDefault("payment-deposit-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-deposit-invalid-character", "&cYou must have an active character in order to deposit money into a payment group.")
        messages.setDefault("payment-deposit-invalid-owner", "&cYou must be an owner of the payment group in order to deposit money.")
        messages.setDefault("payment-deposit-usage", "&cUsage: /payment deposit [group] [amount]")
        messages.setDefault("payment-list-title", "&fPayment groups:")
        messages.setDefault("payment-list-item", "&f- &7\$name &f(&7\$rank&f)")
        messages.setDefault("payment-info-owner", listOf(
            "&7\$name (&a&l\$edit(name)&7)",
            "&7Owners: &f\$owners",
            "&7Members: &f\$members",
            "&7Invites: &f\$invites",
            "&7Amount: &f\$amount &7(&a&l\$edit(amount)&7)",
            "&7Currency: &f\$currency &7(&a&l\$edit(currency)&7)",
            "&7Interval: &f\$interval &7(&a&l\$edit(interval)&7)",
            "&7Last payment time: &f\$last-payment-time",
            "&7Balance: &f\$balance"
        ))
        messages.setDefault("payment-info-not-owner", listOf(
            "&7$name",
            "&7Owners: &f\$owners",
            "&7Members: &f\$members",
            "&7Invites: &f\$invites",
            "&7Amount: &f\$amount",
            "&7Currency: &f\$currency",
            "&7Interval: &f\$interval",
            "&7Last payment time: &f\$last-payment-time",
            "&7Balance: &f\$balance"
        ))
        messages.setDefault("payment-info-invalid-group", "&cThere is no payment group by that name.")
        messages.setDefault("payment-info-usage", "&cUsage: /payment info [group]")
        messages.setDefault("payment-set-amount-invalid-owner", "&cYou must be an owner of the payment group in order to set the amount.")
        messages.setDefault("payment-set-amount-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("payment-set-amount-usage", "&cUsage: /payment set amount [group]")
        messages.setDefault("payment-set-amount-prompt", "&fWhat would you like to set the amount paid out to be? Use negative amounts to make characters pay in to the group instead of being paid. &7(Type cancel to cancel)")
        messages.setDefault("payment-set-amount-invalid-number", "&cYou must specify a number for the amount.")
        messages.setDefault("payment-set-amount-valid", "&aAmount set.")
        messages.setDefault("payment-set-currency-invalid-owner", "&cYou must be an owner of the payment group in order to set the currency.")
        messages.setDefault("payment-set-currency-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("payment-set-currency-usage", "&cUsage: /payment set currency [group]")
        messages.setDefault("payment-set-currency-prompt", "&fWhich currency should transactions be performed in? &7(Type cancel to cancel)")
        messages.setDefault("payment-set-currency-invalid-currency", "&cYou must specify a valid currency.")
        messages.setDefault("payment-set-currency-valid", "&aCurrency set.")
        messages.setDefault("payment-set-interval-invalid-owner", "&cYou must be an owner of the payment group in order to set the name.")
        messages.setDefault("payment-set-interval-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("payment-set-interval-usage", "&cUsage: /payment set interval [group]")
        messages.setDefault("payment-set-interval-prompt", "&fWhat would you like to set the interval between payments to be (in seconds)?  &7(Type cancel to cancel)")
        messages.setDefault("payment-set-interval-invalid-validation", "&cThe interval must be a positive integer.")
        messages.setDefault("payment-set-interval-invalid-number", "&cYou must specify a number for the interval.")
        messages.setDefault("payment-set-interval-valid", "&aInterval set.")
        messages.setDefault("payment-set-name-invalid-owner", "&cYou must be an owner of the payment group in order to set the name.")
        messages.setDefault("payment-set-name-invalid-group", "&cNo payment group by that name exists.")
        messages.setDefault("payment-set-name-invalid-name-already-exists", "&cA payment group by that name already exists.")
        messages.setDefault("payment-set-name-usage", "&cUsage: /payment set name [group]")
        messages.setDefault("payment-set-name-prompt", "&fWhat would you like the name of the payment group to be set to? &7(Type cancel to cancel)")
        messages.setDefault("payment-set-name-valid", "&aName set.")
        messages.setDefault("payment-set-usage", "&cUsage: /payment set [amount|currency|interval|name]")
        messages.setDefault("payment-notification-member-fail-to-pay", "&cYou failed to pay to payment group \"\$group\" on \$date.")
        messages.setDefault("payment-notification-owner-fail-to-pay", "&c\$member failed to pay to payment group \"\$group\" on \$date.")
        messages.setDefault("payment-notification-member-fail-to-be-paid", "&cPayment group \"\$group\" failed to pay you on \$date.")
        messages.setDefault("payment-notification-owner-fail-to-be-paid", "&cPayment group \"\$group\" failed to pay \$member on \$date.")
        messages.setDefault("payment-notification-member-join", "&a\$member joined payment group \"\$group\" on \$date.")
        messages.setDefault("payment-notification-member-leave", "&a\$member left payment group \"\$group\" on \$date.")
        messages.setDefault("payment-notification-invite", "&aYou were invited to join payment group \"\$group\" on \$date.")
        messages.setDefault("payment-notification-kick", "&cYou were kicked from payment group \"\$group\" on \$date.")
        messages.setDefault("no-profile", "&cYour Minecraft profile is not linked to a profile. Please link it on the server's web UI.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-permission-payment-set-amount", "&cYou do not have permission to set payment group amounts.")
        messages.setDefault("no-permission-payment-set-currency", "&cYou do not have permission to set payment group currencies.")
        messages.setDefault("no-permission-payment-set-interval", "&cYou do not have permission to set payment group intervals.")
        messages.setDefault("no-permission-payment-set-name", "&cYou do not have permission to set payment group names.")
        messages.setDefault("no-permission-payment-create", "&cYou do not have permission to create payment groups.")
        messages.setDefault("no-permission-payment-deposit", "&cYou do not have permission to deposit into payment groups.")
        messages.setDefault("no-permission-payment-info", "&cYou do not have permission to view information on payment groups.")
        messages.setDefault("no-permission-payment-invite", "&cYou do not have permission to invite people to payment groups.")
        messages.setDefault("no-permission-payment-join", "&cYou do not have permission to join payment groups.")
        messages.setDefault("no-permission-payment-kick", "&cYou do not have permission to kick people from payment groups.")
        messages.setDefault("no-permission-payment-leave", "&cYou do not have permission to leave payment groups.")
        messages.setDefault("no-permission-payment-list", "&cYou do not have permission to list payment groups.")
        messages.setDefault("no-permission-payment-withdraw", "&cYou do not have permission to withdraw from payment groups.")
    }
}
