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


interface ElysiumCurrencyProvider: ServiceProvider {
    val currencies: Collection<ElysiumCurrency>
    val defaultCurrency: ElysiumCurrency?
    fun getCurrency(id: Int): ElysiumCurrency?
    fun getCurrency(name: String): ElysiumCurrency?
    fun addCurrency(currency: ElysiumCurrency)
    fun removeCurrency(currency: ElysiumCurrency)
}