package com.seventh_root.elysium.players.bukkit

import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.database.table.ElysiumPlayerTable
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import java.sql.SQLException

class ElysiumPlayersBukkit: ElysiumBukkitPlugin() {

    private lateinit var playerProvider: ElysiumPlayerProvider
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        playerProvider = ElysiumPlayerProvider(this)
        serviceProviders = arrayOf<ServiceProvider>(playerProvider)
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(ElysiumPlayerTable(this, database))
    }

}
