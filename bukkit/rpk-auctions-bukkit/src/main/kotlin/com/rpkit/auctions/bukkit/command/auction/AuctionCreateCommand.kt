/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.auctions.bukkit.auction.RPKAuctionProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

/**
 * Auction creation command.
 * Currently does not take any further arguments, instead using conversations to obtain data needed to create the auction.
 */
class AuctionCreateCommand(private val plugin: RPKAuctionsBukkit): CommandExecutor {

    val conversationFactory: ConversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(CurrencyPrompt())
            .withEscapeSequence("cancel")
            .thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.auctions.command.auction.create")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    conversationFactory.buildConversation(sender).begin()
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-auction-create")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class CurrencyPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-currency-prompt")) + "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).currencies
                            .map { currency ->
                                ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-currency-prompt-list-item"))
                                        .replace("\$currency", currency.name)
                            }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-currency-invalid-currency"))
        }

    }

    private inner class CurrencySetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return DurationPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-currency-valid"))
        }

    }

    private inner class DurationPrompt: NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("duration", input.toInt())
            return DurationSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-duration-prompt"))
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() > 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-duration-invalid-number"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-duration-invalid-negative"))
        }
    }

    private inner class DurationSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return StartPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-duration-valid"))
        }

    }

    private inner class StartPricePrompt: NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("start_price", input.toInt())
            return StartPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-start-price-prompt"))
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-start-price-invalid-number"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-start-price-invalid-negative"))
        }

    }

    private inner class StartPriceSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return BuyOutPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-start-price-valid"))
        }

    }

    private inner class BuyOutPricePrompt: NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("buy_out_price", input.toInt())
            return BuyOutPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-buy-out-price-prompt"))
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-buy-out-price-invalid-number"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-buy-out-price-invalid-negative"))
        }

    }

    private inner class BuyOutPriceSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return NoSellPricePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-buy-out-price-valid"))
        }

    }

    private inner class NoSellPricePrompt: NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("no_sell_price", input.toInt())
            return NoSellPriceSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-no-sell-price-prompt"))
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-no-sell-price-invalid-number"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-no-sell-price-invalid-negative"))
        }

    }

    private inner class NoSellPriceSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MinimumBidIncrementPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-no-sell-price-valid"))
        }

    }

    private inner class MinimumBidIncrementPrompt: NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("minimum_bid_increment", input.toInt())
            return MinimumBidIncrementSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-minimum-bid-increment-prompt"))
        }

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-minimum-bid-increment-invalid-number"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-minimum-bid-increment-invalid-negative"))
        }

    }

    private inner class MinimumBidIncrementSetPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AuctionCreatedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-set-minimum-bid-increment-valid"))
        }

    }

    private inner class AuctionCreatedPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val auctionProvider = plugin.core.serviceManager.getServiceProvider(RPKAuctionProvider::class)
            val bukkitPlayer = context.forWhom as Player
            val player = playerProvider.getPlayer(bukkitPlayer)
            val character = characterProvider.getActiveCharacter(player)
            if (character != null) {
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
                auctionProvider.addAuction(auction)
                auction.openBidding()
                auctionProvider.updateAuction(auction)
                bukkitPlayer.inventory.itemInMainHand = null
                context.setSessionData("id", auction.id)
            } else {
                context.setSessionData("id", -1)
            }
            return AuctionIDPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-create-valid"))
        }

    }

    private inner class AuctionIDPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            if (context.getSessionData("id") != -1) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.auction-create-id"))
                        .replace("\$id", context.getSessionData("id").toString())
            } else {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character"))
            }
        }

    }

}