package com.seventh_root.elysium.players.bukkit

import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.database.table.BukkitPlayerTable
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayer
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
import com.seventh_root.elysium.players.bukkit.player.PlayerProvider
import java.sql.SQLException

class ElysiumPlayersBukkit : ElysiumBukkitPlugin() {

    private lateinit var playerProvider: PlayerProvider<BukkitPlayer>
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        playerProvider = BukkitPlayerProvider(this)
        serviceProviders = arrayOf<ServiceProvider>(playerProvider)
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(BukkitPlayerTable(database))
    }

}
