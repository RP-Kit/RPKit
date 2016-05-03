package com.seventh_root.elysium.chat.bukkit

import com.seventh_root.elysium.api.chat.ChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.command.chatchannel.ChatChannelCommand
import com.seventh_root.elysium.chat.bukkit.database.table.BukkitChatChannelTable
import com.seventh_root.elysium.chat.bukkit.listener.AsyncPlayerChatListener
import com.seventh_root.elysium.chat.bukkit.listener.PlayerJoinListener
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException

class ElysiumChatBukkit : ElysiumBukkitPlugin() {

    private lateinit var chatChannelProvider: ChatChannelProvider<BukkitChatChannel>
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        chatChannelProvider = BukkitChatChannelProvider(this)
        serviceProviders = arrayOf<ServiceProvider>(chatChannelProvider)
    }

    override fun registerCommands() {
        getCommand("chatchannel").executor = ChatChannelCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                AsyncPlayerChatListener(this),
                PlayerJoinListener(this))
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(BukkitChatChannelTable(this, database))
    }
}
