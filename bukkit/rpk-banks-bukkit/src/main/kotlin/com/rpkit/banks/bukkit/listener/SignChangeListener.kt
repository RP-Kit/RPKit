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

package com.rpkit.banks.bukkit.listener

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import org.bukkit.ChatColor.GREEN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

/**
 * Sign change listener for bank signs.
 */
class SignChangeListener(private val plugin: RPKBanksBukkit) : Listener {
    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0).equals("[bank]", ignoreCase = true)) {
            event.setLine(0, "$GREEN[bank]")
            if (!event.player.hasPermission("rpkit.banks.sign.bank")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-permission-bank-create"])
                return
            }
            if (!(event.getLine(1).equals("withdraw", ignoreCase = true) || event.getLine(1).equals("deposit", ignoreCase = true) || event.getLine(1).equals("balance", ignoreCase = true))) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["bank-sign-invalid-operation"])
                return
            }
            if (event.getLine(1).equals("balance", ignoreCase = true)) {
                event.setLine(2, "")
            } else {
                event.setLine(2, "1")
            }
            val currencyService = Services[RPKCurrencyService::class.java]
            if (currencyService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-currency-service"])
                return
            }
            val currencyName = event.getLine(3)
            if (currencyName == null || currencyService.getCurrency(currencyName) == null) {
                val defaultCurrency = currencyService.defaultCurrency
                if (defaultCurrency == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.messages["bank-sign-invalid-currency"])
                    return
                } else {
                    event.setLine(3, defaultCurrency.name)
                }
            }
        }
    }
}