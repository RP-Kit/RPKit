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

package com.rpkit.economy.bukkit.currency

import com.rpkit.core.bukkit.util.withDisplayName
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import org.bukkit.Material.GOLD_NUGGET
import org.bukkit.inventory.ItemStack

/**
 * Currency service implementation.
 */
class RPKCurrencyServiceImpl(override val plugin: RPKEconomyBukkit) : RPKCurrencyService {

    override val currencies: List<RPKCurrency> = plugin.config.getConfigurationSection("currencies")
        ?.getKeys(false)
        ?.map { currencyName ->
            RPKCurrencyImpl(
                RPKCurrencyName(currencyName),
                plugin.config.getString("currencies.$currencyName.name-singular") ?: currencyName,
                plugin.config.getString("currencies.$currencyName.name-plural") ?: currencyName,
                plugin.config.getDouble("currencies.$currencyName.rate"),
                plugin.config.getInt("currencies.$currencyName.default-amount"),
                plugin.config.getItemStack("currencies.$currencyName.item") ?: ItemStack(GOLD_NUGGET).withDisplayName(currencyName)
            )
        }
        ?: emptyList()

    override val defaultCurrency: RPKCurrency?
        get() {
            val currencyName = plugin.config.getString("default-currency")
            return if (currencyName != null)
                getCurrency(RPKCurrencyName(currencyName))
            else
                null
        }

    override fun getCurrency(name: RPKCurrencyName): RPKCurrency? {
        return currencies.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }
    }


}
