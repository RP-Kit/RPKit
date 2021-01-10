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
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroupImpl
import com.rpkit.payments.bukkit.group.RPKPaymentGroupName
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MILLIS

/**
 * Payment create command.
 * Creates a payment group.
 */
class PaymentCreateCommand(private val plugin: RPKPaymentsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.payments.command.payment.create")) {
            sender.sendMessage(plugin.messages["no-permission-payment-create"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["payment-create-usage"])
            return true
        }
        val paymentGroupService = Services[RPKPaymentGroupService::class.java]
        if (paymentGroupService == null) {
            sender.sendMessage(plugin.messages["no-payment-group-service"])
            return true
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages["no-currency-service"])
            return true
        }
        val currencyName = plugin.config.getString("payment-groups.defaults.currency")
        val currency = if (currencyName == null)
            currencyService.defaultCurrency
        else
            currencyService.getCurrency(RPKCurrencyName(currencyName))
        val name = args.joinToString(" ")
        if (paymentGroupService.getPaymentGroup(RPKPaymentGroupName(name)) != null) {
            sender.sendMessage(plugin.messages["payment-create-invalid-name-already-exists"])
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
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        val paymentGroup = RPKPaymentGroupImpl(
                plugin,
                name = RPKPaymentGroupName(name),
                amount = plugin.config.getInt("payment-groups.defaults.amount"),
                currency = currency,
                interval = Duration.of(plugin.config.getLong("payment-groups.defaults.interval"), MILLIS),
                lastPaymentTime = LocalDateTime.now(),
                balance = 0
        )
        paymentGroupService.addPaymentGroup(paymentGroup)
        paymentGroup.addOwner(character)
        sender.sendMessage(plugin.messages["payment-create-valid"])
        return true
    }

}
