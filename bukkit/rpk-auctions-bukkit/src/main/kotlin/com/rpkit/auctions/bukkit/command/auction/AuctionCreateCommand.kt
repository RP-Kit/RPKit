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

package com.rpkit.auctions.bukkit.command.auction

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
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
            .thatExcludesNonPlayersWithMessage(plugin.messages.notFromConsole)
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(plugin.messages.operationCancelled)
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.auctions.command.auction.create")) {
            sender.sendMessage(plugin.messages.noPermissionAuctionCreate)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return true
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages.noCurrencyService)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character != null) {
            conversationFactory.buildConversation(sender).begin()
        } else {
            sender.sendMessage(plugin.messages.noCharacter)
        }
        return true
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
            return plugin.messages.auctionSetCurrencyPrompt + "\n" +
                    Services[RPKCurrencyService::class.java]
                            ?.currencies
                            ?.joinToString("\n") { currency ->
                                plugin.messages.auctionSetCurrencyPromptListItem.withParameters(
                                    currency = currency
                                )
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.auctionSetCurrencyInvalidCurrency
        }

    }

    private inner class CurrencySetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return DurationPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetCurrencyValid
        }

    }

    private inner class DurationPrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("duration", input.toInt())
            return DurationSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetDurationPrompt
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() > 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.auctionSetDurationInvalidNumber
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages.auctionSetDurationInvalidNegative
        }
    }

    private inner class DurationSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return StartPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetDurationValid
        }

    }

    private inner class StartPricePrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("start_price", input.toInt())
            return StartPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetStartPricePrompt
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.auctionSetStartPriceInvalidNumber
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages.auctionSetStartPriceInvalidNegative
        }

    }

    private inner class StartPriceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return BuyOutPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetStartPriceValid
        }

    }

    private inner class BuyOutPricePrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("buy_out_price", input.toInt())
            return BuyOutPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetBuyOutPricePrompt
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.auctionSetBuyOutPriceInvalidNumber
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages.auctionSetBuyOutPriceInvalidNegative
        }

    }

    private inner class BuyOutPriceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return NoSellPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetBuyOutPriceValid
        }

    }

    private inner class NoSellPricePrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("no_sell_price", input.toInt())
            return NoSellPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetNoSellPricePrompt
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.auctionSetNoSellPriceInvalidNumber
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages.auctionSetNoSellPriceInvalidNegative
        }

    }

    private inner class NoSellPriceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MinimumBidIncrementPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetNoSellPriceValid
        }

    }

    private inner class MinimumBidIncrementPrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("minimum_bid_increment", input.toInt())
            return MinimumBidIncrementSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetMinimumBidIncrementPrompt
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages.auctionSetMinimumBidIncrementInvalidNumber
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return plugin.messages.auctionSetMinimumBidIncrementInvalidNegative
        }

    }

    private inner class MinimumBidIncrementSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AuctionCreatedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionSetMinimumBidIncrementValid
        }

    }

    private inner class AuctionCreatedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                    ?: return AuctionErrorPrompt(plugin.messages.noMinecraftProfileService)
            val characterService = Services[RPKCharacterService::class.java]
                    ?: return AuctionErrorPrompt(plugin.messages.noCharacterService)
            val auctionService = Services[RPKAuctionService::class.java]
                    ?: return AuctionErrorPrompt(plugin.messages.noAuctionService)
            val bukkitPlayer = context.forWhom as Player
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
                    ?: return AuctionErrorPrompt(plugin.messages.noMinecraftProfile)
            val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
                    ?: return AuctionErrorPrompt(plugin.messages.noCharacter)
            auctionService.createAuction(
                bukkitPlayer.inventory.itemInMainHand,
                context.getSessionData("currency") as RPKCurrency,
                bukkitPlayer.location,
                character,
                (context.getSessionData("duration") as Int) * 3600000L, // 60 mins * 60 secs * 1000 millisecs
                System.currentTimeMillis() + ((context.getSessionData("duration") as Int) * 3600000L),
                context.getSessionData("start_price") as Int,
                context.getSessionData("buy_out_price") as Int,
                context.getSessionData("no_sell_price") as Int,
                context.getSessionData("minimum_bid_increment") as Int
            ).thenAccept { auction ->
                if (auction == null) {
                    bukkitPlayer.sendMessage(plugin.messages.auctionCreateFailed)
                } else {
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        auction.openBidding()
                        auctionService.updateAuction(auction)
                            .thenAccept { updateSuccessful ->
                                if (!updateSuccessful) {
                                    bukkitPlayer.sendMessage(plugin.messages.auctionUpdateFailed)
                                } else {
                                    plugin.server.scheduler.runTask(plugin, Runnable {
                                        bukkitPlayer.inventory.setItemInMainHand(null)
                                    })
                                    bukkitPlayer.sendMessage(plugin.messages.auctionCreateId.withParameters(id = auction.id?.value ?: 0))
                                }
                            }
                    })
                }
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.auctionCreateValid
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