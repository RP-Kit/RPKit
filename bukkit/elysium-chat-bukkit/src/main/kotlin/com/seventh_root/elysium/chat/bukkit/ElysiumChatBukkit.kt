package com.seventh_root.elysium.chat.bukkit

import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.command.chatchannel.ChatChannelCommand
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelListenerTable
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelSpeakerTable
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumChatChannelTable
import com.seventh_root.elysium.chat.bukkit.listener.AsyncPlayerChatListener
import com.seventh_root.elysium.chat.bukkit.listener.PlayerJoinListener
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException

class ElysiumChatBukkit: ElysiumBukkitPlugin() {

    private lateinit var chatChannelProvider: ElysiumChatChannelProvider
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        chatChannelProvider = ElysiumChatChannelProvider(this)
        serviceProviders = arrayOf<ServiceProvider>(chatChannelProvider)
    }

    override fun registerCommands() {
        getCommand("chatchannel").executor = ChatChannelCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                AsyncPlayerChatListener(this),
                PlayerJoinListener(this)
        )
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(ElysiumChatChannelTable(this, database))
        database.addTable(ChatChannelListenerTable(this, database))
        database.addTable(ChatChannelSpeakerTable(this, database))
    }
}
