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

package com.rpkit.economy.bukkit.economy

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import com.rpkit.economy.bukkit.currency.RPKCurrency
import java.util.concurrent.CompletableFuture

/**
 * Provides economy related services.
 */
interface RPKEconomyService : Service {

    /**
     * Gets the balance of a character in the given currency.
     *
     * @param character The character to get the balance of
     * @param currency The currency to get the character's balance of
     * @return The balance of the character in the currency
     */
    fun getBalance(character: RPKCharacter, currency: RPKCurrency): CompletableFuture<Int>

    /**
     * Gets the preloaded balance of a character
     *
     * @param character The character to get the balance of
     * @param currency The currency to get the character's balance of
     * @return If loaded, the balance of the character in the currency, otherwise null
     */
    fun getPreloadedBalance(character: RPKCharacter, currency: RPKCurrency): Int?

    /**
     * Loads the balances of a character
     *
     * @return A [CompletableFuture] that completes once the balances have been loaded
     */
    fun loadBalances(character: RPKCharacter): CompletableFuture<Void>

    /**
     * Unloads the balances of a character
     */
    fun unloadBalances(character: RPKCharacter)

    /**
     * Sets the balance of the character in the given currency to the given amount.
     *
     * @param character The character to set the balance of
     * @param currency The currency to set the balance in
     * @param amount The amount to set the balance to
     */
    fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void>

    /**
     * Transfers money from one character to another.
     *
     * @param from The character to transfer money from
     * @param to The character to transfer money to
     * @param currency The currency to transfer money in
     * @param amount The amount to transfer
     */
    fun transfer(from: RPKCharacter, to: RPKCharacter, currency: RPKCurrency, amount: Int): CompletableFuture<Void>

    /**
     * Gets the richest characters in a particular currency
     *
     * @param currency The currency
     * @param amount The amount of characters to get
     * @return A list containing the amount of richest characters in the currency
     */
    fun getRichestCharacters(currency: RPKCurrency, amount: Int): CompletableFuture<List<RPKCharacter>>

}