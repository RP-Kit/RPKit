/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.selection.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.selection.bukkit.command.WandCommand
import com.rpkit.selection.bukkit.database.table.RPKSelectionTable
import com.rpkit.selection.bukkit.listener.PlayerInteractListener
import com.rpkit.selection.bukkit.selection.RPKSelectionProviderImpl
import org.bstats.bukkit.Metrics


class RPKSelectionBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
        serviceProviders = arrayOf(
                RPKSelectionProviderImpl(this)
        )
    }

    override fun registerListeners() {
        registerListeners(
                PlayerInteractListener(this)
        )
    }

    override fun registerCommands() {
        getCommand("wand")?.setExecutor(WandCommand(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKSelectionTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("not-from-console", "&cYou must be a player to perform that action.")
        messages.setDefault("wand-valid", "&fHere's a wand.")
        messages.setDefault("wand-primary", "&fFirst location set to &7\$world&f, &7\$x&f, &7\$y&f, &7\$z")
        messages.setDefault("wand-secondary", "&fSecond location set to &7\$world&f, &7\$x&f, &7\$y&f, &7\$z")
        messages.setDefault("no-permission-wand", "&cYou do not have permission to obtain a selection wand.")
    }

}