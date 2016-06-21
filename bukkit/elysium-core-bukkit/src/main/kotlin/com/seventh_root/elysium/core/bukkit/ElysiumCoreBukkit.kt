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

package com.seventh_root.elysium.core.bukkit

import com.seventh_root.elysium.core.ElysiumCore
import com.seventh_root.elysium.core.bukkit.listener.PluginEnableListener
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider

import java.sql.SQLException

class ElysiumCoreBukkit: ElysiumBukkitPlugin() {

    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        core = ElysiumCore(logger, Database(config.getString("database.url"), config.getString("database.username"), config.getString("database.password")))
        try {
            createTables(core.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        serviceProviders = arrayOf<ServiceProvider>()
        registerServiceProviders(this)
        registerCommands()
        registerListeners()
    }

    override fun registerListeners() {
        registerListeners(PluginEnableListener(this))
    }

    fun registerServiceProviders(plugin: ElysiumBukkitPlugin) {
        for (provider in plugin.serviceProviders) {
            core.serviceManager.registerServiceProvider(provider)
        }
    }

    fun initializePlugin(elysiumBukkitPlugin: ElysiumBukkitPlugin) {
        elysiumBukkitPlugin.core = core
        try {
            elysiumBukkitPlugin.createTables(core.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        registerServiceProviders(elysiumBukkitPlugin)
        elysiumBukkitPlugin.registerCommands()
        elysiumBukkitPlugin.registerListeners()
        elysiumBukkitPlugin.onPostEnable()
    }

}
