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

package com.rpkit.trade.bukkit.listener

import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.trade.bukkit.RPKTradeBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

/**
 * Sign change listener for trader signs.
 */
class SignChangeListener(private val plugin: RPKTradeBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0) == "[trader]") {
            if (event.player.hasPermission("rpkit.trade.sign.trader.create")) {
                if ((Material.matchMaterial(event.getLine(1)) == null && !event.getLine(1).matches(Regex("\\d+\\s+.*")))
                        || Material.matchMaterial(event.getLine(1).replace(Regex("\\d+\\s+"), "")) == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.core.messages["trader-sign-invalid-material"])
                    return
                }
                if (!event.getLine(2).matches(Regex("\\d+ \\| \\d+"))) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.core.messages["trader-sign-invalid-price"])
                    return
                }
                if (plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(event.getLine(3)) == null) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.core.messages["trader-sign-invalid-currency"])
                    return
                }
                event.setLine(0, GREEN.toString() + "[trader]")
                event.player.sendMessage(plugin.core.messages["trader-sign-valid"])
            } else {
                event.player.sendMessage(plugin.core.messages["no-permission-trader-create"])
            }
        }
    }

}