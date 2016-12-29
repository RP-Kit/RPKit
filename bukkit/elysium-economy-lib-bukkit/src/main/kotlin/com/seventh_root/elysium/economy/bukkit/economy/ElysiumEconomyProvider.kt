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

package com.seventh_root.elysium.economy.bukkit.economy

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency

/**
 * Provides economy related services.
 */
interface ElysiumEconomyProvider: ServiceProvider {

    /**
     * Gets the balance of a character in the given currency.
     *
     * @param character The character to get the balance of
     * @param currency The currency to get the character's balance of
     * @return The balance of the character in the currency
     */
    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int

    /**
     * Sets the balance of the character in the given currency to the given amount.
     *
     * @param character The character to set the balance of
     * @param currency The currency to set the balance in
     * @param amount The amount to set the balance to
     */
    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int)

    /**
     * Transfers money from one character to another.
     *
     * @param from The character to transfer money from
     * @param to The character to transfer money to
     * @param currency The currency to transfer money in
     * @param amount The amount to transfer
     */
    fun transfer(from: ElysiumCharacter, to: ElysiumCharacter, currency: ElysiumCurrency, amount: Int)

    /**
     * Gets the richest characters in a particular currency
     *
     * @param currency The currency
     * @param amount The amount of characters to get
     * @return A list containing the amount of richest characters in the currency
     */
    fun getRichestCharacters(currency: ElysiumCurrency, amount: Int): List<ElysiumCharacter>

}