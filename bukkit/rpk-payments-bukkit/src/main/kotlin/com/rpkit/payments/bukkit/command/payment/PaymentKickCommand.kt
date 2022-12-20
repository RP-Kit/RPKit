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
import java.util.concurrent.CompletableFuture

/**
 * Payment kick command.
 * Kicks a character from a payment group.
 */
class PaymentKickCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.kick")) {
            sender.sendMessage(plugin.messages.noPermissionPaymentKick)
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.paymentKickUsage)
            return true
        }
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages.noPaymentGroupService)
            return true
        }
        paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.dropLast(1).joinToString(" ")))
            .thenAccept getPaymentGroup@{ paymentGroup ->
                if (paymentGroup == null) {
                    sender.sendMessage(plugin.messages.paymentKickInvalidGroup)
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
                val bukkitPlayer = plugin.server.getPlayer(args.last())
                if (bukkitPlayer == null) {
                    sender.sendMessage(plugin.messages.paymentKickInvalidPlayer)
                    return@getPaymentGroup
                }
                val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
                if (minecraftProfile == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfile)
                    return@getPaymentGroup
                }
                val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
                if (character == null) {
                    sender.sendMessage(plugin.messages.paymentKickInvalidCharacter)
                    return@getPaymentGroup
                }
                CompletableFuture.allOf(
                    paymentGroup.removeInvite(character),
                    paymentGroup.removeMember(character)
                ).thenRun {
                    sender.sendMessage(plugin.messages.paymentKickValid)
                    val notificationService = Services[RPKNotificationService::class.java]
                    if (notificationService == null) {
                        sender.sendMessage(plugin.messages.noNotificationService)
                        return@thenRun
                    }
                    val now = LocalDateTime.now()
                    val notificationTitle = plugin.messages.paymentNotificationKickTitle.withParameters(
                        member = character,
                        group = paymentGroup,
                        date = now.atZone(ZoneId.systemDefault())
                    )
                    val notificationMessage = plugin.messages.paymentNotificationKick.withParameters(
                        member = character,
                        group = paymentGroup,
                        date = now.atZone(ZoneId.systemDefault())
                    )
                    val profile = character.profile
                    if (profile != null) {
                        notificationService.createNotification(
                            recipient = profile,
                            title = notificationTitle,
                            content = notificationMessage
                        )
                    }
                }
            }
        return true
    }
}