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

package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.core.service.ServiceProvider

/**
 * Provides currency related operations.
 */
interface ElysiumCurrencyProvider: ServiceProvider {

    /**
     * A collection of all currencies currently managed by this currency provider.
     * The collection is immutable, to add or remove currencies, [addCurrency] or [removeCurrency] should be used.
     */
    val currencies: Collection<ElysiumCurrency>

    /**
     * The default currency in use.
     * In the case where there is no default currency, this may be null. This makes all currency operations require the
     * currency to be specified.
     */
    val defaultCurrency: ElysiumCurrency?

    /**
     * Gets a currency by ID.
     * If there is no currency with the given ID, null is returned.
     *
     * @param id The ID of the currency
     * @return The currency, or null if there is no currency with the given ID
     */
    fun getCurrency(id: Int): ElysiumCurrency?

    /**
     * Gets a currency by name.
     * If there is no currency with the given name, null is returned.
     *
     * @param name The name of the currency
     * @return The currency, or null if there is no currency with the given name
     */
    fun getCurrency(name: String): ElysiumCurrency?

    /**
     * Adds a currency.
     *
     * @param currency The currency to add
     */
    fun addCurrency(currency: ElysiumCurrency)

    /**
     * Removes a currency.
     *
     * @param currency The currency to remove
     */
    fun removeCurrency(currency: ElysiumCurrency)

}