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

package com.seventh_root.elysium.chat.bukkit

import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProviderImpl
import com.seventh_root.elysium.chat.bukkit.command.chatchannel.ChatChannelCommand
import com.seventh_root.elysium.chat.bukkit.command.prefix.PrefixCommand
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelListenerTable
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelSpeakerTable
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumChatChannelTable
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumPrefixTable
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProviderImpl
import com.seventh_root.elysium.chat.bukkit.listener.AsyncPlayerChatListener
import com.seventh_root.elysium.chat.bukkit.listener.PlayerJoinListener
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProviderImpl
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException

class ElysiumChatBukkit: ElysiumBukkitPlugin() {

    private lateinit var chatChannelProvider: ElysiumChatChannelProvider
    private lateinit var ircProvider: ElysiumIRCProvider
    private lateinit var prefixProvider: ElysiumPrefixProvider
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        chatChannelProvider = ElysiumChatChannelProviderImpl(this)
        ircProvider = ElysiumIRCProviderImpl(this)
        prefixProvider = ElysiumPrefixProviderImpl(this)
        serviceProviders = arrayOf(chatChannelProvider, ircProvider, prefixProvider)
    }

    override fun registerCommands() {
        getCommand("chatchannel").executor = ChatChannelCommand(this)
        getCommand("prefix").executor = PrefixCommand(this)
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
        database.addTable(ElysiumPrefixTable(database))
    }
}
