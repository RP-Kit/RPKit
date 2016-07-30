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

package com.seventh_root.elysium.core.bukkit.plugin

import com.seventh_root.elysium.core.ElysiumCore
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.core.web.ElysiumServlet
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.sql.SQLException

abstract class ElysiumBukkitPlugin: JavaPlugin() {

    lateinit var core: ElysiumCore

    open fun onPostEnable() {

    }

    open fun registerCommands() {

    }

    open fun registerListeners() {

    }

    fun registerListeners(vararg listeners: Listener) {
        for (listener in listeners) {
            server.pluginManager.registerEvents(listener, this)
        }
    }

    @Throws(SQLException::class)
    open fun createTables(database: Database) {
    }

    var serviceProviders = arrayOf<ServiceProvider>()
    var servlets = arrayOf<ElysiumServlet>()

}
