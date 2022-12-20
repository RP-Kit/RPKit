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

package com.rpkit.payments.bukkit.command.payment

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroupName
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Payment join command.
 * Joins a payment group.
 */
class PaymentJoinCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.join")) {
            sender.sendMessage(plugin.messages.noPermissionPaymentJoin)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.paymentJoinUsage)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages.noPaymentGroupService)
            return true
        }
        paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.joinToString(" "))).thenAccept getPaymentGroup@{ paymentGroup ->
            if (paymentGroup == null) {
                sender.sendMessage(plugin.messages.paymentJoinInvalidGroup)
                return@getPaymentGroup
            }
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                sender.sendMessage(plugin.messages.noMinecraftProfileService)
                return@getPaymentGroup
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                sender.sendMessage(plugin.messages.noCharacterService)
                return@getPaymentGroup
            }
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
            if (minecraftProfile == null) {
                sender.sendMessage(plugin.messages.noMinecraftProfile)
                return@getPaymentGroup
            }
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
            if (character == null) {
                sender.sendMessage(plugin.messages.paymentJoinInvalidCharacter)
                return@getPaymentGroup
            }
            paymentGroup.invites.thenAccept { invites ->
                if (!invites.contains(character)) {
                    sender.sendMessage(plugin.messages.paymentJoinInvalidInvite)
                    return@thenAccept
                }
                paymentGroup.removeInvite(character)
                paymentGroup.addMember(character)
                sender.sendMessage(plugin.messages.paymentJoinValid)
                val notificationService = Services[RPKNotificationService::class.java]
                if (notificationService == null) {
                    sender.sendMessage(plugin.messages.noNotificationService)
                    return@thenAccept
                }
                val now = LocalDateTime.now()
                val ownerNotificationTitle = plugin.messages.paymentNotificationMemberJoinTitle.withParameters(
                    member = character,
                    group = paymentGroup,
                    date = now.atZone(ZoneId.systemDefault())
                )
                val ownerNotificationMessage = plugin.messages.paymentNotificationMemberJoin.withParameters(
                    member = character,
                    group = paymentGroup,
                    date = now.atZone(ZoneId.systemDefault())
                )
                paymentGroup.owners.thenAccept { owners ->
                    owners.forEach { owner ->
                        val ownerProfile = owner.profile
                        if (ownerProfile != null) {
                            notificationService.createNotification(
                                ownerProfile,
                                ownerNotificationTitle,
                                ownerNotificationMessage
                            )
                        }
                    }
                }
            }
        }
        return true
    }
}