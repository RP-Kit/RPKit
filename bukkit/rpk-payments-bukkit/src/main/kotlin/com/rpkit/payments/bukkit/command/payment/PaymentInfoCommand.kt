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
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroupName
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.format.DateTimeFormatter

/**
 * Payment info command.
 * Displays information on a payment group.
 */
class PaymentInfoCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.info")) {
            sender.sendMessage(plugin.messages["no-permission-payment-info"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["payment-info-usage"])
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
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.joinToString(" "))).thenAccept getPaymentGroup@{ paymentGroup ->
            if (paymentGroup == null) {
                sender.sendMessage(plugin.messages["payment-info-invalid-group"])
                return@getPaymentGroup
            }
            paymentGroup.owners.thenAccept { owners ->
                paymentGroup.members.thenAccept { members ->
                    paymentGroup.invites.thenAccept { invites ->
                        if (owners.contains(character)) {
                            plugin.messages.paymentInfoOwner.withParameters(
                                group = paymentGroup,
                                owners = owners,
                                members = members,
                                invites = invites
                            ).forEach { line ->
                                sender.spigot().sendMessage(*line.toTypedArray())
                            }
                        } else {
                            plugin.messages.paymentInfoNotOwnerMessage.withParameters(
                                group = paymentGroup,
                                owners = owners,
                                members = members,
                                invites = invites
                            ).forEach { line ->
                                sender.spigot().sendMessage(*line.toTypedArray())
                            }
                        }
                    }
                }
            }
        }
        return true
    }
}
