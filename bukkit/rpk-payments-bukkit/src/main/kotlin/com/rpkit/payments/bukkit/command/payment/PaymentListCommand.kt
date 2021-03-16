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
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Payment list command.
 * Lists all payment groups currently involved in.
 */
class PaymentListCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.list")) {
            sender.sendMessage(plugin.messages["no-permission-payment-list"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
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
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages["no-payment-group-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        sender.sendMessage(plugin.messages["payment-list-title"])
        val paymentGroups = paymentGroupService.paymentGroups

        paymentGroups.forEach { paymentGroup ->
            paymentGroup.owners.thenAccept { owners ->
                if (owners.contains(character)) {
                    sender.sendMessage(
                        plugin.messages["payment-list-item", mapOf(
                            "name" to paymentGroup.name.value,
                            "rank" to "Owner"
                        )]
                    )
                }
            }
        }
        paymentGroups.forEach { paymentGroup ->
            paymentGroup.members.thenAccept { members ->
                if (members.contains(character)) {
                    sender.sendMessage(
                        plugin.messages["payment-list-item", mapOf(
                            "name" to paymentGroup.name.value,
                            "rank" to "Member"
                        )]
                    )
                }
            }
        }
        paymentGroups.forEach { paymentGroup ->
            paymentGroup.invites.thenAccept { invites ->
                if (invites.contains(character)) {
                    sender.sendMessage(
                        plugin.messages["payment-list-item", mapOf(
                            "name" to paymentGroup.name.value,
                            "rank" to "Invited"
                        )]
                    )
                }
            }
        }
        return true
    }
}