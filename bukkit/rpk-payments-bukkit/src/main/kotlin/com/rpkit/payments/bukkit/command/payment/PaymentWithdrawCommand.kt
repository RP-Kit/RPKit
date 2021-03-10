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

import com.rpkit.banks.bukkit.bank.RPKBankService
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

/**
 * Payment withdraw command.
 * Withdraws money from a payment group.
 */
class PaymentWithdrawCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.withdraw")) {
            sender.sendMessage(plugin.messages["no-permission-payment-withdraw"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages["payment-withdraw-usage"])
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
        val bankService = Services[RPKBankService::class.java]
        if (bankService == null) {
            sender.sendMessage(plugin.messages["no-bank-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["payment-withdraw-invalid-character"])
            return true
        }
        val paymentGroup = paymentGroupService.getPaymentGroup(RPKPaymentGroupName(args.dropLast(1).joinToString(" ")))
        if (paymentGroup == null) {
            sender.sendMessage(plugin.messages[".payment-withdraw-invalid-group"])
            return true
        }
        if (!paymentGroup.owners.contains(character)) {
            sender.sendMessage(plugin.messages["payment-withdraw-invalid-owner"])
            return true
        }
        val currency = paymentGroup.currency
        if (currency == null) {
            sender.sendMessage(plugin.messages["payment-withdraw-invalid-currency"])
            return true
        }
        try {
            val amount = args.last().toInt()
            if (amount <= 0) {
                sender.sendMessage(plugin.messages["payment-withdraw-invalid-amount"])
                return true
            }
            if (paymentGroup.balance < amount) {
                sender.sendMessage(plugin.messages["payment-withdraw-invalid-balance"])
                return true
            }
            bankService.getBalance(character, currency).thenAccept { bankBalance ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    bankService.setBalance(character, currency, bankBalance + amount).thenRun {
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            paymentGroup.balance = paymentGroup.balance - amount
                            paymentGroupService.updatePaymentGroup(paymentGroup)
                            sender.sendMessage(plugin.messages["payment-withdraw-valid"])
                        })
                    }
                })
            }
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["payment-withdraw-invalid-amount"])
        }
        return true
    }
}
