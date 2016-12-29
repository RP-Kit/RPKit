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

package com.seventh_root.elysium.banks.bukkit.bank

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.database.Entity
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency

/**
 * Represents a bank account for a character in a currency.
 * Each character may have one account in each currency.
 * Unlike their wallet balance, bank balance does not have a cap (or rather, a high enough cap that no one will reach it)
 */
class ElysiumBank(
        override var id: Int = 0,
        val character: ElysiumCharacter,
        val currency: ElysiumCurrency,
        var balance: Int
): Entity