/*
 * Copyright 2023 Ren Binden
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

package com.rpkit.economy.bukkit.command.money

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Money pay command.
 * Pays money to another character from the user's active character.
 */
class MoneyPayCommand(private val plugin: RPKEconomyBukkit) : CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(PlayerPrompt())
            .withEscapeSequence("cancel")
            .thatExcludesNonPlayersWithMessage(plugin.messages["not-from-console"])
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(plugin.messages["operation-cancelled"])
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.economy.command.money.pay")) {
            sender.sendMessage(plugin.messages["no-permission-money-pay"])
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
        val economyService = Services[RPKEconomyService::class.java]
        if (economyService == null) {
            sender.sendMessage(plugin.messages["no-economy-service"])
            return true
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages["no-currency-service"])
            return true
        }
        val fromMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (fromMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val fromCharacter = characterService.getPreloadedActiveCharacter(fromMinecraftProfile)
        if (fromCharacter == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        if (args.isEmpty()) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        val toBukkitPlayer = plugin.server.getPlayer(args[0])
        if (toBukkitPlayer == null) {
            sender.sendMessage(plugin.messages["money-pay-player-invalid-player-offline"])
            return true
        }
        val toMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(toBukkitPlayer)
        if (toMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val toCharacter = characterService.getPreloadedActiveCharacter(toMinecraftProfile)
        if (toCharacter == null) {
            sender.sendMessage(plugin.messages["money-pay-character-invalid-character"])
            return true
        }
        if (args.size <= 1) {
            conversationFactory.buildConversation(sender).begin()
            return true
        }
        try {
            val amount = args[1].toInt()
            if (amount < 0) {
                sender.sendMessage(plugin.messages["money-pay-amount-invalid-amount-negative"])
                return true
            }
            if (args.size <= 2) {
                conversationFactory.buildConversation(sender).begin()
                return true
            }
            var currency = currencyService.getCurrency(RPKCurrencyName(args[2]))
            if (currency == null) {
                currency = currencyService.defaultCurrency
            }
            if (currency == null) {
                sender.sendMessage(plugin.messages["money-pay-currency-invalid-currency"])
                return true
            }
            if (sender.location.distanceSquared(toBukkitPlayer.location) > plugin.config.getDouble("payments.maximum-distance") * plugin.config.getDouble("payments.maximum-distance")) {
                sender.sendMessage(plugin.messages["money-pay-player-invalid-player-distance"])
                return true
            }
            val fromWalletBalance = economyService.getPreloadedBalance(fromCharacter, currency)
            if (fromWalletBalance == null) {
                sender.sendMessage(plugin.messages.noPreloadedBalanceSelf)
                return true
            }
            if (fromWalletBalance < amount) {
                sender.sendMessage(plugin.messages["money-pay-amount-invalid-amount-balance"])
                return true
            }
            val toWalletBalance = economyService.getPreloadedBalance(toCharacter, currency)
            if (toWalletBalance == null) {
                sender.sendMessage(plugin.messages.noPreloadedBalanceOther.withParameters(character = toCharacter))
                return true
            }
            if (toWalletBalance + amount > 1728) {
                sender.sendMessage(plugin.messages["money-pay-amount-invalid-amount-limit"])
                return true
            }
            economyService.transfer(fromCharacter, toCharacter, currency, amount)
            sender.sendMessage(plugin.messages["money-pay-amount-valid"])
            sender.sendMessage(plugin.messages["money-pay-valid", mapOf(
                    "amount" to amount.toString(),
                    "currency" to if (amount == 1) currency.nameSingular else currency.namePlural,
                    "character" to toCharacter.name,
                    "player" to toMinecraftProfile.name
            )])
            toMinecraftProfile.sendMessage(plugin.messages["money-pay-received", mapOf(
                    "amount" to amount.toString(),
                    "currency" to if (amount == 1) currency.nameSingular else currency.namePlural,
                    "character" to fromCharacter.name,
                    "player" to fromMinecraftProfile.name
            )])
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["money-pay-amount-invalid-amount-number"])
        }
        return true
    }

    private inner class PlayerPrompt : PlayerNamePrompt(plugin) {

        override fun acceptValidatedInput(context: ConversationContext, input: Player): Prompt {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return END_OF_CONVERSATION
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(input)
            context.setSessionData("minecraft_profile", minecraftProfile)
            return PlayerSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            if (Services[RPKMinecraftProfileService::class.java] == null) return plugin.messages["no-minecraft-profile-service"]
            return plugin.messages["money-pay-player-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-pay-player-invalid-player-offline"]
        }

    }

    private inner class PlayerSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-pay-player-valid"]
        }

    }

    private inner class CurrencyPrompt : ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return Services[RPKCurrencyService::class.java]?.getCurrency(RPKCurrencyName(input)) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", Services[RPKCurrencyService::class.java]?.getCurrency(RPKCurrencyName(input)))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-pay-currency-prompt"] + "\n" +
                    Services[RPKCurrencyService::class.java]?.currencies
                            ?.joinToString("\n") { currency ->
                                plugin.messages["money-pay-currency-prompt-list-item", mapOf(
                                        "currency" to currency.name.value
                                )]
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            if (Services[RPKCurrencyService::class.java] == null) return plugin.messages["no-currency-service"]
            return plugin.messages["money-pay-currency-invalid-currency"]
        }
    }

    private inner class CurrencySetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-pay-currency-valid"]
        }

    }

    private inner class AmountPrompt : NumericPrompt() {

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() > 0
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("amount", input.toInt())
            return AmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-pay-amount-prompt"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["money-pay-amount-invalid-amount-negative"]
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-pay-amount-invalid-amount-number"]
        }

    }

    private inner class AmountSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneyPayCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-pay-amount-valid"]
        }

    }

    private inner class MoneyPayCompletePrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                    ?: return plugin.messages["no-minecraft-profile-service"]
            val characterService = Services[RPKCharacterService::class.java] ?: return plugin.messages["no-character-service"]
            val economyService = Services[RPKEconomyService::class.java] ?: return plugin.messages["no-economy-service"]
            val fromBukkitPlayer = context.forWhom as Player
            val fromMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(fromBukkitPlayer)
                    ?: return plugin.messages["no-minecraft-profile"]
            val fromCharacter = characterService.getPreloadedActiveCharacter(fromMinecraftProfile)
            val toMinecraftProfile = context.getSessionData("minecraft_profile") as RPKMinecraftProfile
            val toCharacter = characterService.getPreloadedActiveCharacter(toMinecraftProfile)
            val currency = context.getSessionData("currency") as RPKCurrency
            val amount = context.getSessionData("amount") as Int
            if (fromCharacter == null) {
                return plugin.messages["no-character"]
            }
            if (toCharacter == null) {
                return plugin.messages["recipient-no-character"]
            }
            val toBukkitOfflinePlayer = plugin.server.getOfflinePlayer(toMinecraftProfile.minecraftUUID)
            val toBukkitPlayer = toBukkitOfflinePlayer.player
                    ?: return plugin.messages["money-pay-player-invalid-player-distance"]
            if (fromBukkitPlayer.location.distanceSquared(toBukkitPlayer.location) > plugin.config.getDouble("payments.maximum-distance") * plugin.config.getDouble("payments.maximum-distance")) {
                return plugin.messages["money-pay-player-invalid-player-distance"]
            }
            val fromWalletBalance = economyService.getPreloadedBalance(fromCharacter, currency)
                ?: return plugin.messages.noPreloadedBalanceSelf
            if (fromWalletBalance < amount) {
                return plugin.messages["money-pay-amount-invalid-amount-balance"]
            }
            val toWalletBalance = economyService.getPreloadedBalance(toCharacter, currency)
                ?: return plugin.messages.noPreloadedBalanceOther.withParameters(character = toCharacter)
            if (toWalletBalance + amount > 1728) {
                return plugin.messages["money-pay-amount-invalid-amount-limit"]
            }
            economyService.transfer(fromCharacter, toCharacter, currency, amount)
            toMinecraftProfile.sendMessage(plugin.messages["money-pay-received", mapOf(
                    "amount" to amount.toString(),
                    "currency" to if (amount == 1) currency.nameSingular else currency.namePlural,
                    "character" to fromCharacter.name,
                    "player" to fromMinecraftProfile.name
            )])
            return plugin.messages["money-pay-valid", mapOf(
                    "amount" to amount.toString(),
                    "currency" to if (amount == 1) currency.nameSingular else currency.namePlural,
                    "character" to toCharacter.name,
                    "player" to toMinecraftProfile.name
            )]
        }

    }
}