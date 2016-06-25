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

package com.seventh_root.elysium.economy.bukkit

import com.seventh_root.elysium.characters.bukkit.character.field.ElysiumCharacterCardFieldProvider
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.character.MoneyField
import com.seventh_root.elysium.economy.bukkit.command.currency.CurrencyCommand
import com.seventh_root.elysium.economy.bukkit.command.money.MoneyCommand
import com.seventh_root.elysium.economy.bukkit.command.money.MoneyPayCommand
import com.seventh_root.elysium.economy.bukkit.command.money.MoneyWalletCommand
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProviderImpl
import com.seventh_root.elysium.economy.bukkit.database.table.ElysiumCurrencyTable
import com.seventh_root.elysium.economy.bukkit.database.table.ElysiumWalletTable
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProviderImpl
import com.seventh_root.elysium.economy.bukkit.listener.InventoryCloseListener


class ElysiumEconomyBukkit: ElysiumBukkitPlugin() {

    private lateinit var currencyProvider: ElysiumCurrencyProvider
    private lateinit var economyProvider: ElysiumEconomyProvider
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        currencyProvider = ElysiumCurrencyProviderImpl(this)
        economyProvider = ElysiumEconomyProviderImpl(this)
        serviceProviders = arrayOf(
                currencyProvider,
                economyProvider
        )
    }

    override fun onPostEnable() {
        core.serviceManager.getServiceProvider(ElysiumCharacterCardFieldProvider::class.java)
                .characterCardFields.add(MoneyField(this))
    }

    override fun registerCommands() {
        getCommand("money").executor = MoneyCommand(this)
        getCommand("pay").executor = MoneyPayCommand(this)
        getCommand("wallet").executor = MoneyWalletCommand(this)
        getCommand("currency").executor = CurrencyCommand(this)
    }

    override fun registerListeners() {
        registerListeners(InventoryCloseListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(ElysiumCurrencyTable(database, this))
        database.addTable(ElysiumWalletTable(database, this))
    }
}