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

package com.rpkit.economy.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency

class EconomyMessages(plugin: RPKEconomyBukkit) : BukkitMessages(plugin) {

    class CurrencyListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(currency: RPKCurrency) = message.withParameters(
            "currency" to currency.name.value
        )
    }

    class NoPreloadedBalanceOtherMessage(private val message: ParameterizedMessage) {
        fun withParameters(character: RPKCharacter) = message.withParameters(
            "character" to character.name
        )
    }

    val moneyUsage = get("money-usage")
    val moneySubtractProfileNamePrompt = get("money-subtract-profile-name-prompt")
    val moneySubtractProfileDiscriminatorPrompt = get("money-subtract-profile-discriminator-prompt")
    val moneySubtractProfileInvalidProfile = get("money-subtract-profile-invalid-profile")
    val moneySubtractProfileValid = get("money-subtract-profile-valid")
    val moneySubtractCharacterPrompt = get("money-subtract-character-prompt")
    val moneySubtractCharacterPromptListItem = get("money-subtract-character-prompt-list-item")
    val moneySubtractCharacterInvalidCharacter = get("money-subtract-character-invalid-character")
    val moneySubtractCharacterValid = get("money-subtract-character-valid")
    val moneySubtractCurrencyPrompt = get("money-subtract-currency-prompt")
    val moneySubtractCurrencyPromptListItem = get("money-subtract-currency-prompt-list-item")
    val moneySubtractCurrencyInvalidCurrency = get("money-subtract-currency-invalid-currency")
    val moneySubtractCurrencyValid = get("money-subtract-currency-valid")
    val moneySubtractAmountPrompt = get("money-subtract-amount-prompt")
    val moneySubtractAmountInvalidAmountBalance = get("money-subtract-amount-invalid-amount-balance")
    val moneySubtractAmountInvalidAmountNegative = get("money-subtract-amount-invalid-amount-negative")
    val moneySubtractAmountInvalidAmountNumber = get("money-subtract-amount-invalid-amount-number")
    val moneySubtractAmountValid = get("money-subtract-amount-valid")
    val moneySubtractValid = get("money-subtract-valid")
    val moneySubtractUsage = get("money-subtract-usage")
    val moneyAddPlayerInvalidPlayer = get("money-add-player-invalid-player")
    val moneyAddProfileNamePrompt = get("money-add-profile-name-prompt")
    val moneyAddProfileDiscriminatorPrompt = get("money-add-profile-discriminator-prompt")
    val moneyAddProfileInvalidProfile = get("money-add-profile-invalid-profile")
    val moneyAddProfileValid = get("money-add-profile-valid")
    val moneyAddCharacterPrompt = get("money-add-character-prompt")
    val moneyAddCharacterPromptListItem = get("money-add-character-prompt-list-item")
    val moneyAddCharacterInvalidCharacter = get("money-add-character-invalid-character")
    val moneyAddCharacterValid = get("money-add-character-valid")
    val moneyAddCurrencyPrompt = get("money-add-currency-prompt")
    val moneyAddCurrencyPromptListItem = get("money-add-currency-prompt-list-item")
    val moneyAddCurrencyInvalidCurrency = get("money-add-currency-invalid-currency")
    val moneyAddCurrencyValid = get("money-add-currency-valid")
    val moneyAddAmountPrompt = get("money-add-amount-prompt")
    val moneyAddAmountInvalidAmountNegative = get("money-add-amount-invalid-amount-negative")
    val moneyAddAmountInvalidAmountNumber = get("money-add-amount-invalid-amount-number")
    val moneyAddAmountInvalidAmountLimit = get("money-add-amount-invalid-amount-limit")
    val moneyAddAmountValid = get("money-add-amount-valid")
    val moneyAddValid = get("money-add-valid")
    val moneyAddUsage = get("money-add-usage")
    val moneySetProfileNamePrompt = get("money-set-profile-name-prompt")
    val moneySetProfileDiscriminatorPrompt = get("money-set-profile-discriminator-prompt")
    val moneySetProfileInvalidProfile = get("money-set-profile-invalid-profile")
    val moneySetProfileValid = get("money-set-profile-valid")
    val moneySetCharacterPrompt = get("money-set-character-prompt")
    val moneySetCharacterPromptListItem = get("money-set-character-prompt-list-item")
    val moneySetCharacterInvalidCharacter = get("money-set-character-invalid-character")
    val moneySetCharacterValid = get("money-set-character-valid")
    val moneySetCurrencyPrompt = get("money-set-currency-prompt")
    val moneySetCurrencyPromptListItem = get("money-set-currency-prompt-list-item")
    val moneySetCurrencyInvalidCurrency = get("money-set-currency-invalid-currency")
    val moneySetCurrencyValid = get("money-set-currency-valid")
    val moneySetAmountPrompt = get("money-set-amount-prompt")
    val moneySetAmountInvalidAmountNegative = get("money-set-amount-invalid-amount-negative")
    val moneySetAmountInvalidAmountNumber = get("money-set-amount-invalid-amount-number")
    val moneySetAmountInvalidAmountLimit = get("money-set-amount-invalid-amount-limit")
    val moneySetAmountValid = get("money-set-amount-valid")
    val moneySetValid = get("money-set-valid")
    val moneySetUsage = get("money-set-usage")
    val moneyViewProfilePrompt = get("money-view-profile-prompt")
    val moneyViewProfileInvalidProfile = get("money-view-profile-invalid-profile")
    val moneyViewProfileValid = get("money-view-profile-valid")
    val moneyViewCharacterPrompt = get("money-view-character-prompt")
    val moneyViewCharacterPromptListItem = get("money-view-character-prompt-list-item")
    val moneyViewCharacterInvalidCharacter = get("money-view-character-invalid-character")
    val moneyViewCharacterValid = get("money-view-character-valid")
    val moneyViewCurrencyPrompt = get("money-view-currency-prompt")
    val moneyViewCurrencyPromptListItem = get("money-view-currency-prompt-list-item")
    val moneyViewCurrencyInvalidCurrency = get("money-view-currency-invalid-currency")
    val moneyViewCurrencyValid = get("money-view-currency-valid")
    val moneyViewValid = get("money-view-valid")
    val moneyViewValidListItem = get("money-view-valid-list-item")
    val moneyPayPlayerPrompt = get("money-pay-player-prompt")
    val moneyPayPlayerInvalidPlayerOffline = get("money-pay-player-invalid-player-offline")
    val moneyPayPlayerInvalidPlayerDistance = get("money-pay-player-invalid-player-distance")
    val moneyPayPlayerValid = get("money-pay-player-valid")
    val moneyPayCharacterPrompt = get("money-pay-character-prompt")
    val moneyPayCharacterPromptListItem = get("money-pay-character-prompt-list-item")
    val moneyPayCharacterInvalidCharacter = get("money-pay-character-invalid-character")
    val moneyPayCharacterValid = get("money-pay-character-valid")
    val moneyPayCurrencyPrompt = get("money-pay-currency-prompt")
    val moneyPayAmountPrompt = get("money-pay-amount-prompt")
    val moneyPayAmountInvalidAmountBalance = get("money-pay-amount-invalid-amount-balance")
    val moneyPayAmountInvalidAmountNegative = get("money-pay-amount-invalid-amount-negative")
    val moneyPayAmountInvalidAmountNumber = get("money-pay-amount-invalid-amount-number")
    val moneyPayAmountInvalidAmountLimit = get("money-pay-amount-invalid-amount-limit")
    val moneyPayAmountValid = get("money-pay-amount-valid")
    val moneyPayValid = get("money-pay-valid")
    val moneyPayReceived = get("money-pay-received")
    val moneyWalletCurrencyPrompt = get("money-wallet-currency-prompt")
    val moneyWalletCurrencyPromptListItem = get("money-wallet-currency-prompt-list-item")
    val moneyWalletCurrencyInvalidCurrency = get("money-wallet-currency-invalid-currency")
    val moneyWalletCurrencyValid = get("money-wallet-currency-valid")
    val moneyWalletValid = get("money-wallet-valid")
    val currencyUsage = get("currency-usage")
    val currencyListTitle = get("currency-list-title")
    val currencyListItem = getParameterized("currency-list-item").let(::CurrencyListItemMessage)
    val dynexchangeSignInvalidFormatFrom = get("dynexchange-sign-invalid-format-from")
    val dynexchangeSignInvalidCurrencyFrom = get("dynexchange-sign-invalid-currency-from")
    val dynexchangeSignInvalidFormatTo = get("dynexchange-sign-invalid-format-to")
    val dynexchangeSignInvalidCurrencyTo = get("dynexchange-sign-invalid-currency-to")
    val exchangeSignInvalidFormatFrom = get("exchange-sign-invalid-format-from")
    val exchangeSignInvalidCurrencyFrom = get("exchange-sign-invalid-currency-from")
    val exchangeSignInvalidCurrencyTo = get("exchange-sign-invalid-currency-to")
    val exchangeValid = get("exchange-valid")
    val exchangeInvalidWalletBalanceTooHigh = get("exchange-invalid-wallet-balance-too-high")
    val exchangeInvalidWalletBalanceTooLow = get("exchange-invalid-wallet-balance-too-low")
    val exchangeInvalidFormat = get("exchange-invalid-format")
    val notFromConsole = get("not-from-console")
    val operationCancelled = get("operation-cancelled")
    val noPermissionMoneySubtract = get("no-permission-money-subtract")
    val noPermissionMoneyAdd = get("no-permission-money-add")
    val noPermissionMoneySet = get("no-permission-money-set")
    val noPermissionMoneyViewSelf = get("no-permission-money-view-self")
    val noPermissionMoneyViewOther = get("no-permission-money-view-other")
    val noPermissionMoneyPay = get("no-permission-money-pay")
    val noPermissionMoneyWallet = get("no-permission-money-wallet")
    val noPermissionCurrencyList = get("no-permission-currency-list")
    val noPermissionDynexchangeCreate = get("no-permission-dynexchange-create")
    val noPermissionExchangeCreate = get("no-permission-exchange-create")
    val noCharacter = get("no-character")
    val recipientNoCharacter = get("recipient-no-character")
    val noProfile = get("no-profile")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noPreloadedBalanceSelf = get("no-preloaded-balance-self")
    val noPreloadedBalanceOther = getParameterized("no-preloaded-balance-other").let(::NoPreloadedBalanceOtherMessage)
    val noEconomyService = get("no-economy-service")
    val noCurrencyService = get("no-currency-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
}