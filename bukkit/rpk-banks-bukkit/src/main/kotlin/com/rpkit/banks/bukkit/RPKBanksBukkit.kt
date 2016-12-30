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
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database

/**
 * RPK banks plugin default implementation.
 */
class RPKBanksBukkit: RPKBukkitPlugin() {

    private lateinit var bankProvider: RPKBankProvider

    override fun onEnable() {
        saveDefaultConfig()
        bankProvider = RPKBankProviderImpl(this)
        serviceProviders = arrayOf(
                bankProvider
        )
    }

    override fun registerListeners() {
        registerListeners(SignChangeListener(this), PlayerInteractListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKBankTable(database, this))
    }

}