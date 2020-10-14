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

package com.rpkit.banks.bukkit.bank

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import com.rpkit.economy.bukkit.currency.RPKCurrency

/**
 * Represents a bank service.
 * Banks allow storage of unlimited currency, as opposed to the wallet in the economy plugin,
 * so for large transactions it may be required to retrieve money from the bank.
 * Each character is currently assumed to have a single global bank account per currency, which may be accessed at any
 * bank for that currency.
 * A bank service stores the balance for the character, and has convenience methods to withdraw and deposit money
 * to the character's wallet.
 */
interface RPKBankService : Service {
    fun getBalance(character: RPKCharacter, currency: RPKCurrency): Int
    fun setBalance(character: RPKCharacter, currency: RPKCurrency, amount: Int)
    fun deposit(character: RPKCharacter, currency: RPKCurrency, amount: Int)
    fun withdraw(character: RPKCharacter, currency: RPKCurrency, amount: Int)
    fun getRichestCharacters(currency: RPKCurrency, amount: Int): List<RPKCharacter>
}