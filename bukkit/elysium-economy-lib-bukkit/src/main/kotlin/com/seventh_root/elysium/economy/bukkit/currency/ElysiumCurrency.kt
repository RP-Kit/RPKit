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

import com.seventh_root.elysium.core.database.Entity
import org.bukkit.Material
import java.lang.Math.floor

/**
 * Represents a currency.
 */
interface ElysiumCurrency: Entity {

    /**
     * The name of the currency.
     */
    var name: String

    /**
     * The singular form of the name of the currency.
     * Used when referring to one of the currency.
     */
    var nameSingular: String

    /**
     * The plural form of the currency.
     * Used when referring to anything except one of the currency.
     */
    var namePlural: String

    /**
     * The rate of conversion of the currency.
     */
    var rate: Double

    /**
     * The default amount of the currency owned by characters upon starting.
     */
    var defaultAmount: Int

    /**
     * The material used to represent the currency as a physical item.
     */
    var material: Material

    /**
     * Converts an amount of the currency to another currency.
     *
     * @param amount The amount to convert
     * @param currency The currency to convert to
     * @return The amount converted into the currency
     */
    fun convert(amount: Int, currency: ElysiumCurrency): Int {
        return floor((amount.toDouble() / rate) * currency.rate).toInt()
    }

}
