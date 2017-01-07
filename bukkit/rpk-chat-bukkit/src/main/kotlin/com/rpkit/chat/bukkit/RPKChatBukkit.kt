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

package com.rpkit.chat.bukkit

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProviderImpl
import com.rpkit.chat.bukkit.chatchannel.directed.*
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.LogComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.UndirectedFormatComponent
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupProviderImpl
import com.rpkit.chat.bukkit.command.chatchannel.ChatChannelCommand
import com.rpkit.chat.bukkit.command.chatgroup.ChatGroupCommand
import com.rpkit.chat.bukkit.command.listchatchannels.ListChatChannelsCommand
import com.rpkit.chat.bukkit.command.message.MessageCommand
import com.rpkit.chat.bukkit.command.mute.MuteCommand
import com.rpkit.chat.bukkit.command.reply.ReplyCommand
import com.rpkit.chat.bukkit.command.snoop.SnoopCommand
import com.rpkit.chat.bukkit.command.unmute.UnmuteCommand
import com.rpkit.chat.bukkit.database.table.*
import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import com.rpkit.chat.bukkit.irc.RPKIRCProviderImpl
import com.rpkit.chat.bukkit.listener.AsyncPlayerChatListener
import com.rpkit.chat.bukkit.listener.PlayerCommandPreprocessListener
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteProvider
import com.rpkit.chat.bukkit.prefix.RPKPrefixProvider
import com.rpkit.chat.bukkit.prefix.RPKPrefixProviderImpl
import com.rpkit.chat.bukkit.snooper.RPKSnooperProviderImpl
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

/**
 * RPK chat plugin default implementation.
 */
class RPKChatBukkit: RPKBukkitPlugin() {

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
            val ircProvider = RPKIRCProviderImpl(this)
            val prefixProvider = RPKPrefixProviderImpl(this)
            val chatChannelProvider = RPKChatChannelProviderImpl(this)
            val chatChannelMuteProvider = RPKChatChannelMuteProvider(this)
            val chatChannelSpeakerProvider = RPKChatChannelSpeakerProvider(this)
            val chatGroupProvider = RPKChatGroupProviderImpl(this)
            val snooperProvider =  RPKSnooperProviderImpl(this)
            serviceProviders = arrayOf(
                    ircProvider,
                    prefixProvider,
                    chatChannelProvider,
                    chatChannelMuteProvider,
                    chatChannelSpeakerProvider,
                    chatGroupProvider,
                    snooperProvider
            )
            registerChatChannelPermissions(chatChannelProvider)
            registerPrefixPermissions(prefixProvider)
        } else {
            val prefixProvider = RPKPrefixProviderImpl(this)
            val chatChannelProvider = RPKChatChannelProviderImpl(this)
            val chatChannelMuteProvider = RPKChatChannelMuteProvider(this)
            val chatChannelSpeakerProvider = RPKChatChannelSpeakerProvider(this)
            val chatGroupProvider = RPKChatGroupProviderImpl(this)
            val snooperProvider =  RPKSnooperProviderImpl(this)
            serviceProviders = arrayOf(
                    prefixProvider,
                    chatChannelProvider,
                    chatChannelMuteProvider,
                    chatChannelSpeakerProvider,
                    chatGroupProvider,
                    snooperProvider
            )
            registerChatChannelPermissions(chatChannelProvider)
            registerPrefixPermissions(prefixProvider)
        }
    }

    override fun onDisable() {
        if (config.getBoolean("irc.enabled")) {
            core.serviceManager.getServiceProvider(RPKIRCProvider::class).ircBot.sendIRC().quitServer(config.getString("messages.irc-quit"))
        }
    }

    override fun registerCommands() {
        getCommand("chatchannel").executor = ChatChannelCommand(this)
        getCommand("mute").executor = MuteCommand(this)
        getCommand("unmute").executor = UnmuteCommand(this)
        getCommand("listchatchannels").executor = ListChatChannelsCommand(this)
        getCommand("chatgroup").executor = ChatGroupCommand(this)
        getCommand("message").executor = MessageCommand(this)
        getCommand("reply").executor = ReplyCommand(this)
        getCommand("snoop").executor = SnoopCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                AsyncPlayerChatListener(this),
                PlayerCommandPreprocessListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKChatChannelMuteTable(database, this))
        database.addTable(RPKChatChannelSpeakerTable(database, this))
        database.addTable(RPKChatGroupTable(database, this))
        database.addTable(ChatGroupInviteTable(database, this))
        database.addTable(ChatGroupMemberTable(database, this))
        database.addTable(LastUsedChatGroupTable(database, this))
        database.addTable(RPKSnooperTable(database, this))
    }

    private fun registerChatChannelPermissions(chatChannelProvider: RPKChatChannelProvider) {
        chatChannelProvider.chatChannels.forEach { chatChannel ->
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.command.chatchannel.${chatChannel.name}",
                    "Allows speaking in ${chatChannel.name}",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.command.mute.${chatChannel.name}",
                    "Allows muting ${chatChannel.name}",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.command.unmute.${chatChannel.name}",
                    "Allows unmuting ${chatChannel.name}",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.listen.${chatChannel.name}",
                    "Allows listening to ${chatChannel.name}",
                    PermissionDefault.OP
            ))
        }
    }

    private fun registerPrefixPermissions(prefixProvider: RPKPrefixProvider) {
        prefixProvider.prefixes.forEach { prefix ->
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.prefix.${prefix.name}",
                    "Gives the player the prefix ${prefix.name}",
                    PermissionDefault.FALSE
            ))
        }
    }

}