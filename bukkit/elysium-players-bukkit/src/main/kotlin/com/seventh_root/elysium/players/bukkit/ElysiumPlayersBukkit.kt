package com.seventh_root.elysium.players.bukkit

import com.seventh_root.elysium.api.player.PlayerProvider
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.database.table.BukkitPlayerTable
import java.sql.SQLException

class ElysiumPlayersBukkit : ElysiumBukkitPlugin() {

    private var playerProvider: PlayerProvider<BukkitPlayer>? = null
    override var serviceProviders: Array<ServiceProvider>? = null

    override fun onEnable() {
        playerProvider = BukkitPlayerProvider(this)
        serviceProviders = arrayOf<ServiceProvider>(playerProvider as PlayerProvider<BukkitPlayer>)
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(BukkitPlayerTable(database))
    }

}
