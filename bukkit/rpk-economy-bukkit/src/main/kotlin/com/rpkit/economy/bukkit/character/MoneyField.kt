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

package com.rpkit.economy.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.HideableCharacterCardField
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.database.table.RPKMoneyHiddenTable
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import java.util.concurrent.CompletableFuture

/**
 * Character card field for money.
 * Shows money for all currencies, separated by ", "
 */
class MoneyField(val plugin: RPKEconomyBukkit) : HideableCharacterCardField {

    override val name = "money"
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return isHidden(character).thenApply { hidden ->
            if (hidden) {
                "[HIDDEN]"
            } else {
                val economyService = Services[RPKEconomyService::class.java] ?: return@thenApply plugin.messages["no-economy-service"]
                val currencyService = Services[RPKCurrencyService::class.java] ?: return@thenApply plugin.messages["no-currency-service"]
                currencyService.currencies
                    .joinToString(", ") { currency ->
                        val balance = economyService.getPreloadedBalance(character, currency)
                        "$balance ${if (balance == 1) currency.nameSingular else currency.namePlural}"
                    }
            }
        }
    }

    override fun isHidden(character: RPKCharacter): CompletableFuture<Boolean> {
        return plugin.database.getTable(RPKMoneyHiddenTable::class.java)[character].thenApply { it != null }
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean): CompletableFuture<Void> {
        val moneyHiddenTable = plugin.database.getTable(RPKMoneyHiddenTable::class.java)
        return if (hidden) {
            moneyHiddenTable[character].thenAcceptAsync { moneyHidden ->
                if (moneyHidden == null) {
                    moneyHiddenTable.insert(RPKMoneyHidden(character = character)).join()
                }
            }
        } else {
            moneyHiddenTable[character].thenAccept { moneyHidden ->
                if (moneyHidden != null) {
                    moneyHiddenTable.delete(moneyHidden).join()
                }
            }
        }
    }

}
