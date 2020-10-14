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

package com.rpkit.trade.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.trade.bukkit.RPKTradeBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

/**
 * Sign change listener for trader signs.
 */
class SignChangeListener(private val plugin: RPKTradeBukkit) : Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0) != "[trader]") return
        if (!event.player.hasPermission("rpkit.trade.sign.trader.create")) {
            event.player.sendMessage(plugin.messages["no-permission-trader-create"])
            return
        }
        if ((Material.matchMaterial(event.getLine(1) ?: "") == null
                        && (event.getLine(1)?.matches(Regex("\\d+\\s+.*")) != true))
                || Material.matchMaterial(event.getLine(1)?.replace(Regex("\\d+\\s+"), "") ?: "") == null) {
            event.block.breakNaturally()
            event.player.sendMessage(plugin.messages["trader-sign-invalid-material"])
            return
        }
        if (event.getLine(2)?.matches(Regex("\\d+ \\| \\d+")) != true) {
            event.block.breakNaturally()
            event.player.sendMessage(plugin.messages["trader-sign-invalid-price"])
            return
        }
        val currencyService = Services[RPKCurrencyService::class]
        if (currencyService == null) {
            event.block.breakNaturally()
            event.player.sendMessage(plugin.messages["no-currency-service"])
            return
        }
        if (currencyService.getCurrency(event.getLine(3) ?: "") == null) {
            event.block.breakNaturally()
            event.player.sendMessage(plugin.messages["trader-sign-invalid-currency"])
            return
        }
        event.setLine(0, "$GREEN[trader]")
        event.player.sendMessage(plugin.messages["trader-sign-valid"])
    }

}