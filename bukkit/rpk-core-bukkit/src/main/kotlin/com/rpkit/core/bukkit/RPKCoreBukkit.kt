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

package com.rpkit.core.bukkit

import com.rpkit.core.RPKCore
import com.rpkit.core.bukkit.listener.PluginEnableListener
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.bukkit.servlet.IndexServlet
import com.rpkit.core.bukkit.servlet.StaticServlet
import com.rpkit.core.database.Database
import com.rpkit.core.service.ServiceProvider
import com.rpkit.core.web.NavigationLink
import com.rpkit.core.web.Web
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.sql.SQLException

/**
 * RPK's core, Bukkit implementation.
 * Allows RPK to function on Bukkit.
 */
class RPKCoreBukkit: RPKBukkitPlugin() {

    lateinit var servletContext: ServletContextHandler

    override fun onEnable() {
        saveDefaultConfig()
        val webServer = Server(config.getInt("web-server.port"))
        servletContext = ServletContextHandler()
        webServer.handler = servletContext
        core = RPKCore(
                logger,
                Database(config.getString("database.url"), config.getString("database.username"), config.getString("database.password")),
                Web(webServer, mutableListOf(NavigationLink("Home", "/")))
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

    /**
     * Registers the service providers of a plugin.
     *
     * @param plugin The plugin
     */
    fun registerServiceProviders(plugin: RPKBukkitPlugin) {
        for (provider in plugin.serviceProviders) {
            core.serviceManager.registerServiceProvider(provider)
        }
    }

    /**
     * Registers the servlets of the plugin.
     *
     * @param plugin The plugin
     */
    fun registerServlets(plugin: RPKBukkitPlugin) {
        for (servlet in plugin.servlets) {
            servletContext.addServlet(ServletHolder(servlet), servlet.url)
        }
    }

    /**
     * Initializes an RPK plugin.
     *
     * @param rpkitBukkitPlugin The plugin to initialize
     */
    fun initializePlugin(rpkitBukkitPlugin: RPKBukkitPlugin) {
        rpkitBukkitPlugin.core = core
        rpkitBukkitPlugin.messages = BukkitMessages(rpkitBukkitPlugin)
        rpkitBukkitPlugin.setDefaultMessages()
        try {
            rpkitBukkitPlugin.createTables(core.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        registerServiceProviders(rpkitBukkitPlugin)
        registerServlets(rpkitBukkitPlugin)
        rpkitBukkitPlugin.registerCommands()
        rpkitBukkitPlugin.registerListeners()
        rpkitBukkitPlugin.onPostEnable()
    }

}
