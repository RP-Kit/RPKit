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

package com.rpkit.shops.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.shops.bukkit.command.RestockCommand
import com.rpkit.shops.bukkit.database.table.RPKShopCountTable
import com.rpkit.shops.bukkit.listener.BlockBreakListener
import com.rpkit.shops.bukkit.listener.InventoryClickListener
import com.rpkit.shops.bukkit.listener.PlayerInteractListener
import com.rpkit.shops.bukkit.listener.SignChangeListener
import com.rpkit.shops.bukkit.shopcount.RPKShopCountProviderImpl

/**
 * RPK shops plugin default implementation.
 */
class RPKShopsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKShopCountProviderImpl(this)
        )
    }

    override fun registerListeners() {
        registerListeners(
                SignChangeListener(this),
                BlockBreakListener(this),
                PlayerInteractListener(this),
                InventoryClickListener(this)
        )
    }

    override fun registerCommands() {
        getCommand("restock").executor = RestockCommand(this)
    }

    override fun createTables(database: Database) {
        database.addTable(RPKShopCountTable(database, this))
    }
}