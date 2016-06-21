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

import com.seventh_root.elysium.core.database.TableRow
import org.bukkit.Material


class ElysiumCurrency(
        override var id: Int = 0,
        var name: String,
        var nameSingular: String,
        var namePlural: String,
        var rate: Double,
        var defaultAmount: Int,
        var material: Material
): TableRow {
    fun convert(amount: Double, currency: ElysiumCurrency): Double {
        return (amount / rate) * currency.rate;
    }
}