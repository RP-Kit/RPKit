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

package com.rpkit.banks.bukkit

import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.banks.bukkit.bank.RPKBankProviderImpl
import com.rpkit.banks.bukkit.database.table.RPKBankTable
import com.rpkit.banks.bukkit.listener.PlayerInteractListener
import com.rpkit.banks.bukkit.listener.SignChangeListener
import com.rpkit.banks.bukkit.servlet.BankServlet
import com.rpkit.banks.bukkit.servlet.BanksServlet
import com.rpkit.banks.bukkit.servlet.CharacterServlet
import com.rpkit.banks.bukkit.servlet.StaticServlet
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.web.NavigationLink

/**
 * RPK banks plugin default implementation.
 */
class RPKBanksBukkit: RPKBukkitPlugin() {

    private lateinit var bankProvider: RPKBankProvider

    override fun onEnable() {
        bankProvider = RPKBankProviderImpl(this)
        serviceProviders = arrayOf(
                bankProvider
        )
        servlets = arrayOf(
                BanksServlet(this),
                CharacterServlet(this),
                BankServlet(this),
                StaticServlet(this),
                com.rpkit.banks.bukkit.servlet.api.v1.BankAPIServlet(this)
        )
    }

    override fun onPostEnable() {
        core.web.navigationBar.add(NavigationLink("Banks", "/banks/"))
    }

    override fun registerListeners() {
        registerListeners(SignChangeListener(this), PlayerInteractListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKBankTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("bank-sign-invalid-operation", "&cThat's not a valid operation. Please use withdraw, deposit, or balance. (Line 2)")
        messages.setDefault("bank-sign-invalid-currency", "&cThat''s not a valid currency. Please use a valid currency. (Line 4)")
        messages.setDefault("bank-withdraw-invalid-wallet-full", "&cYou cannot withdraw that amount, it would not fit in your wallet.")
        messages.setDefault("bank-withdraw-invalid-not-enough-money", "&cYou cannot withdraw that amount, your bank balance is not high enough.")
        messages.setDefault("bank-withdraw-valid", "&aWithdrew \$amount \$currency. Wallet balance: \$wallet-balance. Bank balance: \$bank-balance.")
        messages.setDefault("bank-deposit-invalid-not-enough-money", "&cYou cannot deposit that amount, your wallet balance is not high enough.")
        messages.setDefault("bank-deposit-valid", "&aDeposited \$amount \$currency. Wallet balance: \$wallet-balance. Bank balance: \$bank-balance.")
        messages.setDefault("bank-balance-valid", "&aBalance: \$amount \$currency")
        messages.setDefault("no-permission-bank-create", "&cYou do not have permission to create banks.")
    }

}