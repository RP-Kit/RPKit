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

package com.rpkit.economy.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.economy.bukkit.character.MoneyField
import com.rpkit.economy.bukkit.command.currency.CurrencyCommand
import com.rpkit.economy.bukkit.command.money.MoneyCommand
import com.rpkit.economy.bukkit.command.money.MoneyPayCommand
import com.rpkit.economy.bukkit.command.money.MoneyWalletCommand
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.currency.RPKCurrencyProviderImpl
import com.rpkit.economy.bukkit.database.table.MoneyHiddenTable
import com.rpkit.economy.bukkit.database.table.RPKCurrencyTable
import com.rpkit.economy.bukkit.database.table.RPKWalletTable
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProviderImpl
import com.rpkit.economy.bukkit.listener.InventoryCloseListener
import com.rpkit.economy.bukkit.listener.PluginEnableListener

/**
 * RPK economy plugin default implementation.
 */
class RPKEconomyBukkit: RPKBukkitPlugin() {

    private lateinit var currencyProvider: RPKCurrencyProvider
    private lateinit var economyProvider: RPKEconomyProvider
    private var moneyFieldInitialised: Boolean = false

    override fun onEnable() {
        saveDefaultConfig()
        currencyProvider = RPKCurrencyProviderImpl(this)
        economyProvider = RPKEconomyProviderImpl(this)
        serviceProviders = arrayOf(
                currencyProvider,
                economyProvider
        )
    }

    override fun onPostEnable() {
        attemptCharacterCardFieldInitialisation()
    }

    fun attemptCharacterCardFieldInitialisation() {
        if (!moneyFieldInitialised) {
            try {
                core.serviceManager.getServiceProvider(RPKCharacterCardFieldProvider::class)
                        .characterCardFields.add(MoneyField(this))
                moneyFieldInitialised = true
            } catch (ignore: UnregisteredServiceException) {}
        }
    }

    override fun registerCommands() {
        getCommand("money").executor = MoneyCommand(this)
        getCommand("pay").executor = MoneyPayCommand(this)
        getCommand("wallet").executor = MoneyWalletCommand(this)
        getCommand("currency").executor = CurrencyCommand(this)
    }

    override fun registerListeners() {
        registerListeners(InventoryCloseListener(this), PluginEnableListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKCurrencyTable(database, this))
        database.addTable(RPKWalletTable(database, this))
        database.addTable(MoneyHiddenTable(database, this))
    }
}