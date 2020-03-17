/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.core.bukkit.event.provider.RPKBukkitServiceProviderReadyEvent
import com.rpkit.core.bukkit.listener.PluginEnableListener
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.bukkit.servlet.IndexServlet
import com.rpkit.core.bukkit.servlet.StaticServlet
import com.rpkit.core.database.Database
import com.rpkit.core.web.NavigationLink
import com.rpkit.core.web.Web
import org.bstats.bukkit.Metrics
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jooq.SQLDialect
import java.sql.SQLException

/**
 * RPK's core, Bukkit implementation.
 * Allows RPK to function on Bukkit.
 */
class RPKCoreBukkit: RPKBukkitPlugin() {

    lateinit var servletContext: ServletContextHandler

    override fun onEnable() {
        Metrics(this, 4371)
        saveDefaultConfig()
        val webServer = Server(config.getInt("web-server.port"))
        servletContext = ServletContextHandler()
        servletContext.sessionHandler = SessionHandler()
        webServer.handler = servletContext
        val databaseUrl = config.getString("database.url")
        val databaseUsername = config.getString("database.username")
        val databasePassword = config.getString("database.password")
        val databaseDialect = config.getString("database.dialect")
        if (databaseUrl == null) {
            logger.severe("No database URL is set. Disabling.")
            isEnabled = false
            return
        }
        if (databaseDialect == null) {
            logger.severe("No database dialect is set. Disabling.")
            isEnabled = false
            return
        }
        core = RPKCore(
                logger,
                Database(
                        databaseUrl,
                        databaseUsername,
                        databasePassword,
                        SQLDialect.valueOf(databaseDialect)
                ),
                Web(webServer, config.getString("web-server.title") ?: "RPKit", mutableListOf(NavigationLink("Home", "/")))
        )
        try {
            createTables(core.database)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        serviceProviders = arrayOf()
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
            server.pluginManager.callEvent(RPKBukkitServiceProviderReadyEvent(provider))
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
