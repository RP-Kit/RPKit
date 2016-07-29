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
import com.seventh_root.elysium.core.bukkit.servlet.IndexServlet
import com.seventh_root.elysium.core.bukkit.servlet.StaticServlet
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.core.web.NavigationLink
import com.seventh_root.elysium.core.web.Web
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.sql.SQLException

class ElysiumCoreBukkit: ElysiumBukkitPlugin() {

    lateinit var servletContext: ServletContextHandler

    override fun onEnable() {
        saveDefaultConfig()
        val webServer = Server(config.getInt("web-server.port"))
        servletContext = ServletContextHandler()
        webServer.handler = servletContext
        core = ElysiumCore(
                logger,
                Database(config.getString("database.url"), config.getString("database.username"), config.getString("database.password")),
                Web(webServer, arrayOf(NavigationLink("Home", "/")))
        )
        try {
            createTables(core.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        serviceProviders = arrayOf<ServiceProvider>()
        servlets = arrayOf(IndexServlet(this), StaticServlet(this))
        registerServiceProviders(this)
        registerCommands()
        registerListeners()
        registerServlets(this)
    }

    override fun registerListeners() {
        registerListeners(PluginEnableListener(this))
    }

    fun registerServiceProviders(plugin: ElysiumBukkitPlugin) {
        for (provider in plugin.serviceProviders) {
            core.serviceManager.registerServiceProvider(provider)
        }
    }

    fun registerServlets(plugin: ElysiumBukkitPlugin) {
        for (servlet in plugin.servlets) {
            servletContext.addServlet(ServletHolder(servlet), servlet.url)
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
