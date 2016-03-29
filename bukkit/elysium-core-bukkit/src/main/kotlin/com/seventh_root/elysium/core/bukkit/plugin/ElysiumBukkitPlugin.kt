package com.seventh_root.elysium.core.bukkit.plugin

import com.seventh_root.elysium.core.ElysiumCore
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

import java.sql.SQLException

abstract class ElysiumBukkitPlugin : JavaPlugin() {

    open var core: ElysiumCore? = null
        set(core) {
            if (this.core != null) {
                logger.warning("There was an attempt to redefine the ElysiumCore instance.")
                return
            }
            field = core
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

    abstract var serviceProviders: Array<ServiceProvider>?

}
