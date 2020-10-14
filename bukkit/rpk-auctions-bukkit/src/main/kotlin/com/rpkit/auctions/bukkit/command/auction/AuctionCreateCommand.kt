/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.auctions.bukkit.command.auction

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionImpl
import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Auction creation command.
 * Currently does not take any further arguments, instead using conversations to obtain data needed to create the auction.
 */
class AuctionCreateCommand(private val plugin: RPKAuctionsBukkit) : CommandExecutor {

    val conversationFactory: ConversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(CurrencyPrompt())
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
        if (!sender.hasPermission("rpkit.auctions.command.auction.create")) {
            sender.sendMessage(plugin.messages["no-permission-auction-create"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val currencyService = Services[RPKCurrencyService::class]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages["no-currency-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character != null) {
            conversationFactory.buildConversation(sender).begin()
        } else {
            sender.sendMessage(plugin.messages["no-character"])
        }
        return true
    }

    private inner class CurrencyPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return Services[RPKCurrencyService::class]?.getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", Services[RPKCurrencyService::class]?.getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-currency-prompt"] + "\n" +
                    Services[RPKCurrencyService::class]
                            ?.currencies
                            ?.joinToString("\n") { currency ->
                                plugin.messages["auction-set-currency-prompt-list-item", mapOf(
                                        Pair("currency", currency.name)
                                )]
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["auction-set-currency-invalid-currency"]
        }

    }

    private inner class CurrencySetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return DurationPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-currency-valid"]
        }

    }

    private inner class DurationPrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("duration", input.toInt())
            return DurationSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-duration-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() > 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["auction-set-duration-invalid-number"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["auction-set-duration-invalid-negative"]
        }
    }

    private inner class DurationSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return StartPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-duration-valid"]
        }

    }

    private inner class StartPricePrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("start_price", input.toInt())
            return StartPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-start-price-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["auction-set-start-price-invalid-number"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["auction-set-start-price-invalid-negative"]
        }

    }

    private inner class StartPriceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return BuyOutPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-start-price-valid"]
        }

    }

    private inner class BuyOutPricePrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("buy_out_price", input.toInt())
            return BuyOutPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-buy-out-price-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["auction-set-buy-out-price-invalid-number"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["auction-set-buy-out-price-invalid-negative"]
        }

    }

    private inner class BuyOutPriceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return NoSellPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-buy-out-price-valid"]
        }

    }

    private inner class NoSellPricePrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("no_sell_price", input.toInt())
            return NoSellPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-no-sell-price-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["auction-set-no-sell-price-invalid-number"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["auction-set-no-sell-price-invalid-negative"]
        }

    }

    private inner class NoSellPriceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MinimumBidIncrementPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-no-sell-price-valid"]
        }

    }

    private inner class MinimumBidIncrementPrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("minimum_bid_increment", input.toInt())
            return MinimumBidIncrementSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-minimum-bid-increment-prompt"]
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["auction-set-minimum-bid-increment-invalid-number"]
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages["auction-set-minimum-bid-increment-invalid-negative"]
        }

    }

    private inner class MinimumBidIncrementSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AuctionCreatedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-set-minimum-bid-increment-valid"]
        }

    }

    private inner class AuctionCreatedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class]
                    ?: return AuctionErrorPrompt(plugin.messages["no-minecraft-profile-service"])
            val characterService = Services[RPKCharacterService::class]
                    ?: return AuctionErrorPrompt(plugin.messages["no-character-service"])
            val auctionService = Services[RPKAuctionService::class]
                    ?: return AuctionErrorPrompt(plugin.messages["no-auction-service"])
            val bukkitPlayer = context.forWhom as Player
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
                    ?: return AuctionErrorPrompt(plugin.messages["no-minecraft-profile"])
            val character = characterService.getActiveCharacter(minecraftProfile)
                    ?: return AuctionErrorPrompt(plugin.messages["no-character"])
            val auction = RPKAuctionImpl(
                    plugin,
                    item = bukkitPlayer.inventory.itemInMainHand,
                    currency = context.getSessionData("currency") as RPKCurrency,
                    location = bukkitPlayer.location,
                    character = character,
                    duration = (context.getSessionData("duration") as Int) * 3600000L, // 60 mins * 60 secs * 1000 millisecs
                    endTime = System.currentTimeMillis() + ((context.getSessionData("duration") as Int) * 3600000L),
                    startPrice = context.getSessionData("start_price") as Int,
                    buyOutPrice = context.getSessionData("buy_out_price") as Int,
                    noSellPrice = context.getSessionData("no_sell_price") as Int,
                    minimumBidIncrement = context.getSessionData("minimum_bid_increment") as Int
            )
            if (!auctionService.addAuction(auction)) {
                return AuctionErrorPrompt(plugin.messages["auction-create-failed"])
            }
            auction.openBidding()
            if (!auctionService.updateAuction(auction)) {
                return AuctionErrorPrompt(plugin.messages["auction-update-failed"])
            }
            bukkitPlayer.inventory.setItemInMainHand(null)
            context.setSessionData("id", auction.id)
            return AuctionIDPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-create-valid"]
        }

    }

    private inner class AuctionIDPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["auction-create-id", mapOf(
                    "id" to context.getSessionData("id").toString()
            )]
        }

    }

    private inner class AuctionErrorPrompt(val errorMessage: String) : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return errorMessage
        }
    }

}