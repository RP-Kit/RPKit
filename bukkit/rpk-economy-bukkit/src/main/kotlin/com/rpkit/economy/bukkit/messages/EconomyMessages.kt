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

    val currencyUsage = get("currency-usage")
    val currencyListTitle = get("currency-list-title")
    val currencyListItem = getParameterized("currency-list-item").let(::CurrencyListItemMessage)
    val noPermissionCurrencyList = get("no-permission-currency-list")
    val noCurrencyService = get("no-currency-service")

}