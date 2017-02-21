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
            core.serviceManager.getServiceProvider(RPKIRCProvider::class).ircBot.sendIRC().quitServer(messages["irc-quit"])
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

    override fun setDefaultMessages() {
        messages.setDefault("chatchannel-valid", "&aNow speaking in \$channel.")
        messages.setDefault("chatchannel-invalid-chatchannel", "&cNo chat channel by that name exists.")
        messages.setDefault("chatchannel-usage", "&cUsage: /chatchannel [channel]")
        messages.setDefault("mute-valid", "&aMuted \$channel.")
        messages.setDefault("mute-invalid-chatchannel", "&cNo chat channel by that name exists.")
        messages.setDefault("mute-usage", "&cUsage: /mute [channel]")
        messages.setDefault("unmute-valid", "&aUnmuted \$channel.")
        messages.setDefault("unmute-invalid-chatchannel", "&cNo chat channel by that name exists.")
        messages.setDefault("unmute-usage", "&cUsage: /unmute [channel]")
        messages.setDefault("listchatchannels-title", "&fChat channels:")
        messages.setDefault("listchatchannels-item", "&f- \$color\$channel &f(&a&l\$mute&f)")
        messages.setDefault("irc-register-valid", "Registered. Please check your email for the verification code to complete the verification process.")
        messages.setDefault("irc-register-invalid-email-invalid", "You did not specify a valid email.")
        messages.setDefault("irc-register-invalid-email-not-specified", "You must specify an email.")
        messages.setDefault("irc-verify-valid", "Attempted to verify. If you correctly entered the verification code, I should now be verified.")
        messages.setDefault("irc-verify-invalid-verification-code-not-specified", "You must specify the registration code from your email.")
        messages.setDefault("irc-quit", "Server shut down.")
        messages.setDefault("irc-list-title", "Online players: ")
        messages.setDefault("irc-list-item", "\$player, ")
        messages.setDefault("snoop-usage", "&cUsage: /snoop [on|off|check]")
        messages.setDefault("snoop-enabled", "&aSnooping enabled.")
        messages.setDefault("snoop-already-enabled", "&cSnooping was already enabled.")
        messages.setDefault("snoop-disabled", "&aSnooping disabled.")
        messages.setDefault("snoop-already-disabled", "&cSnooping was already disabled.")
        messages.setDefault("snoop-check-on", "&aSnooping is enabled.")
        messages.setDefault("snoop-check-off", "&aSnooping is disabled.")
        messages.setDefault("chat-group-create-valid", "&aChat group created. Use /chatgroup invite \$group [player] to invite players.")
        messages.setDefault("chat-group-create-invalid-reserved", "&cChat groups starting with _pm_ are reserved. Please choose a different name.")
        messages.setDefault("chat-group-create-invalid-taken", "&cA chat group by that name already exists. Please choose a different name.")
        messages.setDefault("chat-group-create-usage", "&cUsage: /chatgroup create [name]")
        messages.setDefault("chat-group-disband-valid", "&aChat group \$group disbanded.")
        messages.setDefault("chat-group-disband-invalid-nonexistent", "&cNo chat group by that name exists.")
        messages.setDefault("chat-group-disband-usage", "&cUsage: /chatgroup disband [name]")
        messages.setDefault("chat-group-invite-received", "&aYou have been invited to chat group \$group. Use /chatgroup join \$group to join.")
        messages.setDefault("chat-group-invite-valid", "&aYou invited \$player to the chat group \$group.")
        messages.setDefault("chat-group-invite-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("chat-group-invite-invalid-not-a-member", "&cYou are not a member of that chat group.")
        messages.setDefault("chat-group-invite-invalid-chat-group", "&cNo chat group by that name exists.")
        messages.setDefault("chat-group-invite-usage", "&cUsage: /chatgroup invite [name] [player]")
        messages.setDefault("chat-group-join-received", "&a\$player joined \$group.")
        messages.setDefault("chat-group-join-valid", "&aJoined chat group \$group.")
        messages.setDefault("chat-group-join-invalid-no-invite", "&cYou have not been invited to that chat group.")
        messages.setDefault("chat-group-join-invalid-chat-group", "&cNo chat group by that name exists.")
        messages.setDefault("chat-group-join-usage", "&cUsage: /chatgroup join [name]")
        messages.setDefault("chat-group-leave-valid", "&aLeft chat group \$group.")
        messages.setDefault("chat-group-leave-invalid-not-a-member", "&cYou are not a member of that chat group.")
        messages.setDefault("chat-group-leave-invalid-chat-group", "&cNo chat group by that name exists.")
        messages.setDefault("chat-group-leave-usage", "&cUsage: /chatgroup leave [name]")
        messages.setDefault("chat-group-message-invalid-not-a-member", "&cYou are not a member of that chat group.")
        messages.setDefault("chat-group-message-invalid-chat-group", "&cNo chat group by that name exists.")
        messages.setDefault("chat-group-message-usage", "&cUsage: /chatgroup message [name] [message]")
        messages.setDefault("chat-group-members-list-title", "&aMembers:")
        messages.setDefault("chat-group-members-list-item", "&f- &7\$player")
        messages.setDefault("chat-group-invitations-list-title", "&aPending invitations:")
        messages.setDefault("chat-group-invitations-list-item", "&f- &7\$player")
        messages.setDefault("chat-group-members-invalid-chat-group", "&cNo chat group by that name exists.")
        messages.setDefault("chat-group-members-usage", "&cUsage: /chatgroup members [name]")
        messages.setDefault("chat-group-usage", "&cUsage: /chatgroup [create|disband|invite|join|leave|message|players]")
        messages.setDefault("reply-usage", "&cUsage: /reply [message]")
        messages.setDefault("reply-invalid-chat-group", "&cYou have not used a chat group or private messaged anyone recently.")
        messages.setDefault("message-invalid-target", "&cNo chat group or player by that name exists.")
        messages.setDefault("message-invalid-self", "&cYou cannot message yourself.")
        messages.setDefault("message-usage", "&cUsage: /message [target] [message]")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("no-character", "&cYou do not currently have an active character. Please create one with /character new, or switch to an old one using /character switch.")
        messages.setDefault("no-chat-channel", "&cYou are not currently speaking in a chat channel. Please list channels with /listchatchannels and then speak in one using /chatchannel [channel].")
        messages.setDefault("no-permission-chatchannel", "&cYou do not have permission to speak in \$channel.")
        messages.setDefault("no-permission-listchatchannels", "&cYou do not have permission to list chat channels.")
        messages.setDefault("no-permission-mute", "&cYou do not have permission to mute \$channel.")
        messages.setDefault("no-permission-unmute", "&cYou do not have permission to unmute \$channel.")
        messages.setDefault("no-permission-snoop-on", "&cYou do not have permission to enable snooping.")
        messages.setDefault("no-permission-snoop-off", "&cYou do not have permission to disable snooping.")
        messages.setDefault("no-permission-snoop-check", "&cYou do not have permission to check whether you are snooping.")
        messages.setDefault("no-permission-chat-group", "&cYou do not have permission to use chat group commands.")
        messages.setDefault("no-permission-chat-group-create", "&cYou do not have permission to create chat groups.")
        messages.setDefault("no-permission-chat-group-disband", "&cYou do not have permission to disband chat groups.")
        messages.setDefault("no-permission-chat-group-invite", "&cYou do not have permission to invite people to chat groups.")
        messages.setDefault("no-permission-chat-group-join", "&cYou do not have permission to join chat groups.")
        messages.setDefault("no-permission-chat-group-leave", "&cYou do not have permission to leave chat groups.")
        messages.setDefault("no-permission-chat-group-members", "&cYou do not have permission to list chat group members.")
        messages.setDefault("no-permission-chat-group-message", "&cYou do not have permission to message chat groups.")
        messages.setDefault("no-permission-message", "&cYou do not have permission to send private messages.")
        messages.setDefault("no-permission-reply", "&cYou do not have permission to reply to messages.")
        messages.setDefault("command-snoop", "&2[command] \$sender-player: \$command")
    }

}