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

package com.rpkit.auctions.bukkit.messages

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.economy.bukkit.currency.RPKCurrency
import org.bukkit.Material

class AuctionsMessages(plugin: RPKAuctionsBukkit) : BukkitMessages(plugin) {

    class AuctionSetCurrencyPromptListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(currency: RPKCurrency) = message.withParameters("currency" to currency.name.value)
    }

    class AuctionCreateIdMessage(private val message: ParameterizedMessage) {
        fun withParameters(id: Int) = message.withParameters("id" to id.toString())
    }

    class AuctionItemReceivedMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            amount: Int,
            itemType: Material,
            auctionId: Int,
            character: RPKCharacter
        ) = message.withParameters(
            "amount" to amount.toString(),
            "item_type" to itemType.toString().toLowerCase().replace('_', ' '),
            "auction_id" to auctionId.toString(),
            "character" to character.name
        )
    }

    class AuctionItemReturnedMessage(private val message: ParameterizedMessage) {
        fun withParameters(amount: Int, itemType: Material, auctionId: Int) = message.withParameters(
            "amount" to amount.toString(),
            "item_type" to itemType.toString().toLowerCase().replace('_', ' '),
            "auction_id" to auctionId.toString()
        )
    }

    class BidInvalidNotHighEnoughMessage(private val message: ParameterizedMessage) {
        fun withParameters(amount: Int) = message.withParameters(
            "amount" to amount.toString()
        )
    }

    class BidValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(currencyAmount: Int, currency: RPKCurrency, itemAmount: Int, itemType: Material) =
            message.withParameters(
                "currency_amount" to currencyAmount.toString(),
                "currency" to currency.name.value,
                "item_amount" to itemAmount.toString(),
                "item_type" to itemType.toString()
            )
    }

    class BidCreatedMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            character: RPKCharacter,
            currencyAmount: Int,
            currency: RPKCurrency,
            auction: RPKAuction,
            itemAmount: Int,
            itemType: Material
        ) = message.withParameters(
            "character" to character.name,
            "currency_amount" to currencyAmount.toString(),
            "currency" to currency.name.value,
            "auction_id" to auction.id.toString(),
            "item_amount" to itemAmount.toString(),
            "item_type" to itemType.toString().toLowerCase().replace('_', ' ')
        )
    }

    val auctionUsage = get("auction-usage")
    val auctionSetCurrencyPrompt = get("auction-set-currency-prompt")
    val auctionSetCurrencyPromptListItem = getParameterized("auction-set-currency-prompt-list-item")
        .let(::AuctionSetCurrencyPromptListItemMessage)
    val auctionSetCurrencyInvalidCurrency = get("auction-set-currency-invalid-currency")
    val auctionSetCurrencyValid = get("auction-set-currency-valid")
    val auctionSetDurationPrompt = get("auction-set-duration-prompt")
    val auctionSetDurationInvalidNumber = get("auction-set-duration-invalid-number")
    val auctionSetDurationInvalidNegative = get("auction-set-duration-invalid-negative")
    val auctionSetDurationValid = get("auction-set-duration-valid")
    val auctionSetStartPricePrompt = get("auction-set-start-price-prompt")
    val auctionSetStartPriceInvalidNumber = get("auction-set-start-price-invalid-number")
    val auctionSetStartPriceInvalidNegative = get("auction-set-start-price-invalid-negative")
    val auctionSetStartPriceValid = get("auction-set-start-price-valid")
    val auctionSetBuyOutPricePrompt = get("auction-set-buy-out-price-prompt")
    val auctionSetBuyOutPriceInvalidNumber = get("auction-set-buy-out-price-invalid-number")
    val auctionSetBuyOutPriceInvalidNegative = get("auction-set-buy-out-price-invalid-negative")
    val auctionSetBuyOutPriceValid = get("auction-set-buy-out-price-valid")
    val auctionSetNoSellPricePrompt = get("auction-set-no-sell-price-prompt")
    val auctionSetNoSellPriceInvalidNumber = get("auction-set-no-sell-price-invalid-number")
    val auctionSetNoSellPriceInvalidNegative = get("auction-set-no-sell-price-invalid-negative")
    val auctionSetNoSellPriceValid = get("auction-set-no-sell-price-valid")
    val auctionSetMinimumBidIncrementPrompt = get("auction-set-minimum-bid-increment-prompt")
    val auctionSetMinimumBidIncrementInvalidNumber = get("auction-set-minimum-bid-increment-invalid-number")
    val auctionSetMinimumBidIncrementInvalidNegative = get("auction-set-minimum-bid-increment-invalid-negative")
    val auctionSetMinimumBidIncrementValid = get("auction-set-minimum-bid-increment-valid")
    val auctionCreateValid = get("auction-create-valid")
    val auctionCreateId = getParameterized("auction-create-id")
        .let(::AuctionCreateIdMessage)
    val auctionItemReceived = getParameterized("auction-item-received")
        .let(::AuctionItemReceivedMessage)
    val auctionItemReturned = getParameterized("auction-item-returned")
        .let(::AuctionItemReturnedMessage)
    val bidUsage = get("bid-usage")
    val bidInvalidNotHighEnough = getParameterized("bid-invalid-not-high-enough")
        .let(::BidInvalidNotHighEnoughMessage)
    val bidInvalidNotEnoughMoney = get("bid-invalid-not-enough-money")
    val bidInvalidAuctionNotOpen = get("bid-invalid-auction-not-open")
    val bidInvalidAuctionNotExistent = get("bid-invalid-auction-not-existent")
    val bidInvalidAmountNotANumber = get("bid-invalid-amount-not-a-number")
    val bidInvalidAuctionIdNotANumber = get("bid-invalid-auction-id-not-a-number")
    val bidInvalidTooFarAway = get("bid-invalid-too-far-away")
    val bidValid = getParameterized("bid-valid")
        .let(::BidValidMessage)
    val bidCreated = getParameterized("bid-created")
        .let(::BidCreatedMessage)
    val auctionSignInvalidIdNotANumber = get("auction-sign-invalid-id-not-a-number")
    val auctionSignInvalidAuctionDoesNotExist = get("auction-sign-invalid-auction-does-not-exist")
    val noCharacter = get("no-character")
    val noMinecraftProfile = get("no-minecraft-profile")
    val notFromConsole = get("not-from-console")
    val operationCancelled = get("operation-cancelled")
    val noPermissionBid = get("no-permission-bid")
    val noPermissionAuctionCreate = get("no-permission-auction-create")
    val noPermissionAuctionSignCreate = get("no-permission-auction-sign-create")
    val auctionCreateFailed = get("auction-create-failed")
    val auctionUpdateFailed = get("auction-update-failed")
    val auctionDeleteFailed = get("auction-delete-failed")
    val bidCreateFailed = get("bid-create-failed")
    val bidUpdateFailed = get("bid-update-failed")
    val bidDeleteFailed = get("bid-delete-failed")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noCurrencyService = get("no-currency-service")
    val noEconomyService = get("no-economy-service")
    val noAuctionService = get("no-auction-service")

}