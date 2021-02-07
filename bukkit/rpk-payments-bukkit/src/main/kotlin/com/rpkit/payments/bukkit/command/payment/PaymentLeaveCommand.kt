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

package com.rpkit.payments.bukkit.command.payment

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroupName
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationImpl
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.time.LocalDateTime

/**
 * Payment leave command.
 * Leaves a payment group.
 */
class PaymentLeaveCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.leave")) {
            sender.sendMessage(plugin.messages["no-permission-payment-leave"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["payment-leave-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages["no-payment-group-service"])
            return true
        }
        val paymentGroup = paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.joinToString(" ")))
        if (paymentGroup == null) {
            sender.sendMessage(plugin.messages["payment-leave-invalid-group"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["payment-leave-invalid-character"])
            return true
        }
        if (!paymentGroup.members.contains(character)) {
            sender.sendMessage(plugin.messages["payment-leave-invalid-member"])
            return true
        }
        paymentGroup.removeMember(character)
        sender.sendMessage(plugin.messages["payment-leave-valid"])
        val paymentNotificationService = Services[RPKPaymentNotificationService::class.java]
        if (paymentNotificationService == null) {
            sender.sendMessage(plugin.messages["no-payment-notification-service"])
            return true
        }
        val now = LocalDateTime.now()
        val ownerNotificationMessage = plugin.messages["payment-notification-member-leave", mapOf(
                "member" to character.name,
                "group" to paymentGroup.name.value,
                "date" to dateFormat.format(now)
        )]
        paymentGroup.owners.forEach { owner ->
            if (owner.minecraftProfile?.isOnline != true) {
                paymentNotificationService.addPaymentNotification(
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
        return true
    }
}