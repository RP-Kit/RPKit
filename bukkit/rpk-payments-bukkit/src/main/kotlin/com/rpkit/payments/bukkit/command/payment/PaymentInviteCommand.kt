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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Payment invite command.
 * Invites a character to a payment group.
 */
class PaymentInviteCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.invite")) {
            sender.sendMessage(plugin.messages.noPermissionPaymentInvite)
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.paymentInviteUsage)
            return true
        }
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages.noPaymentGroupService)
            return true
        }
        paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.dropLast(1).joinToString(" "))).thenAccept getPaymentGroup@{ paymentGroup ->
            if (paymentGroup == null) {
                sender.sendMessage(plugin.messages.paymentInviteInvalidGroup)
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
            val bukkitPlayer = plugin.server.getOfflinePlayer(args.last())
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
            if (minecraftProfile == null) {
                sender.sendMessage(plugin.messages.noMinecraftProfile)
                return@getPaymentGroup
            }
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
            if (character == null) {
                sender.sendMessage(plugin.messages.paymentInviteInvalidCharacter)
                return@getPaymentGroup
            }
            paymentGroup.addInvite(character).thenRun {
                sender.sendMessage(plugin.messages.paymentInviteValid)
                val notificationService = Services[RPKNotificationService::class.java]
                if (notificationService == null) {
                    sender.sendMessage(plugin.messages.noNotificationService)
                    return@thenRun
                }
                val now = LocalDateTime.now()
                val notificationTitle = plugin.messages.paymentNotificationInviteTitle.withParameters(
                    member = character,
                    group = paymentGroup,
                    date = now.atZone(ZoneId.systemDefault())
                )
                val notificationMessage = plugin.messages.paymentNotificationInvite.withParameters(
                    member = character,
                    group = paymentGroup,
                    date = now.atZone(ZoneId.systemDefault())
                )
                if (minecraftProfile.isOnline) { // If online
                    minecraftProfile.sendMessage(notificationMessage)
                } else { // If offline
                    val profile = character.profile
                    if (profile != null) {
                        notificationService.createNotification(
                            profile,
                            notificationTitle,
                            notificationMessage
                        )
                    }
                }
            }
        }
        return true
    }


}