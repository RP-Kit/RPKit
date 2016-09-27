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

import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannelProviderImpl
import com.seventh_root.elysium.chat.bukkit.chatchannel.directed.*
import com.seventh_root.elysium.chat.bukkit.chatchannel.undirected.IRCComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.undirected.LogComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.undirected.UndirectedFormatComponent
import com.seventh_root.elysium.chat.bukkit.chatgroup.ElysiumChatGroupProviderImpl
import com.seventh_root.elysium.chat.bukkit.command.chatchannel.ChatChannelCommand
import com.seventh_root.elysium.chat.bukkit.command.listchatchannels.ListChatChannelsCommand
import com.seventh_root.elysium.chat.bukkit.command.mute.MuteCommand
import com.seventh_root.elysium.chat.bukkit.command.unmute.UnmuteCommand
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumChatChannelMuteTable
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumChatChannelSpeakerTable
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProviderImpl
import com.seventh_root.elysium.chat.bukkit.listener.AsyncPlayerChatListener
import com.seventh_root.elysium.chat.bukkit.listener.PlayerCommandPreprocessListener
import com.seventh_root.elysium.chat.bukkit.mute.ElysiumChatChannelMuteProvider
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProviderImpl
import com.seventh_root.elysium.chat.bukkit.snooper.ElysiumSnooperProviderImpl
import com.seventh_root.elysium.chat.bukkit.speaker.ElysiumChatChannelSpeakerProvider
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import org.bukkit.configuration.serialization.ConfigurationSerialization


class ElysiumChatBukkit: ElysiumBukkitPlugin() {

    override fun onEnable() {
        ConfigurationSerialization.registerClass(DirectedFormatComponent::class.java, "DirectedFormatComponent")
        ConfigurationSerialization.registerClass(GarbleComponent::class.java, "GarbleComponent")
        ConfigurationSerialization.registerClass(RadiusFilterComponent::class.java, "RadiusFilterComponent")
        ConfigurationSerialization.registerClass(SendMessageComponent::class.java, "SendMessageComponent")
        ConfigurationSerialization.registerClass(SnoopComponent::class.java, "SnoopComponent")
        ConfigurationSerialization.registerClass(IRCComponent::class.java, "IRCComponent")
        ConfigurationSerialization.registerClass(LogComponent::class.java, "LogComponent")
        ConfigurationSerialization.registerClass(UndirectedFormatComponent::class.java, "UndirectedFormatComponent")
        saveDefaultConfig()
        if (config.getBoolean("irc.enabled")) {
            serviceProviders = arrayOf(
                    ElysiumIRCProviderImpl(this),
                    ElysiumPrefixProviderImpl(this),
                    ElysiumChatChannelProviderImpl(this),
                    ElysiumChatChannelMuteProvider(this),
                    ElysiumChatChannelSpeakerProvider(this),
                    ElysiumChatGroupProviderImpl(this),
                    ElysiumSnooperProviderImpl(this)
            )
        } else {
            serviceProviders = arrayOf(
                    ElysiumPrefixProviderImpl(this),
                    ElysiumChatChannelProviderImpl(this),
                    ElysiumChatChannelMuteProvider(this),
                    ElysiumChatChannelSpeakerProvider(this),
                    ElysiumChatGroupProviderImpl(this),
                    ElysiumSnooperProviderImpl(this)
            )
        }
    }

    override fun onDisable() {
        if (config.getBoolean("irc.enabled")) {
            core.serviceManager.getServiceProvider(ElysiumIRCProvider::class).ircBot.sendIRC().quitServer(config.getString("messages.irc-quit"))
        }
    }

    override fun registerCommands() {
        getCommand("chatchannel").executor = ChatChannelCommand(this)
        getCommand("mute").executor = MuteCommand(this)
        getCommand("unmute").executor = UnmuteCommand(this)
        getCommand("listchatchannels").executor = ListChatChannelsCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                AsyncPlayerChatListener(this),
                PlayerCommandPreprocessListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(ElysiumChatChannelMuteTable(database, this))
        database.addTable(ElysiumChatChannelSpeakerTable(database, this))
    }

}