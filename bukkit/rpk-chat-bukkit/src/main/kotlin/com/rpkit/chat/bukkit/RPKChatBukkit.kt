/*
 * Copyright 2020 Ren Binden
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

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelServiceImpl
import com.rpkit.chat.bukkit.chatchannel.directed.postformat.SendMessageComponent
import com.rpkit.chat.bukkit.chatchannel.directed.postformat.SnoopComponent
import com.rpkit.chat.bukkit.chatchannel.directed.preformat.DrunkenSlurComponent
import com.rpkit.chat.bukkit.chatchannel.directed.preformat.GarbleComponent
import com.rpkit.chat.bukkit.chatchannel.directed.preformat.LanguageComponent
import com.rpkit.chat.bukkit.chatchannel.directed.preformat.RadiusFilterComponent
import com.rpkit.chat.bukkit.chatchannel.format.click.CopyToClipboardClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.OpenFileClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.OpenURLClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.RunCommandClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.SuggestCommandClickAction
import com.rpkit.chat.bukkit.chatchannel.format.hover.ShowTextHoverAction
import com.rpkit.chat.bukkit.chatchannel.format.part.ChannelPart
import com.rpkit.chat.bukkit.chatchannel.format.part.MessagePart
import com.rpkit.chat.bukkit.chatchannel.format.part.ReceiverCharacterNamePart
import com.rpkit.chat.bukkit.chatchannel.format.part.ReceiverPrefixPart
import com.rpkit.chat.bukkit.chatchannel.format.part.ReceiverProfileNamePart
import com.rpkit.chat.bukkit.chatchannel.format.part.SenderCharacterNamePart
import com.rpkit.chat.bukkit.chatchannel.format.part.SenderPrefixPart
import com.rpkit.chat.bukkit.chatchannel.format.part.SenderProfileNamePart
import com.rpkit.chat.bukkit.chatchannel.format.part.TextPart
import com.rpkit.chat.bukkit.chatchannel.undirected.DiscordComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.LogComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.UndirectedFormatComponent
import com.rpkit.chat.bukkit.chatchannel.undirected.WebComponent
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupServiceImpl
import com.rpkit.chat.bukkit.command.chatchannel.ChatChannelCommand
import com.rpkit.chat.bukkit.command.chatgroup.ChatGroupCommand
import com.rpkit.chat.bukkit.command.listchatchannels.ListChatChannelsCommand
import com.rpkit.chat.bukkit.command.message.MessageCommand
import com.rpkit.chat.bukkit.command.mute.MuteCommand
import com.rpkit.chat.bukkit.command.reply.ReplyCommand
import com.rpkit.chat.bukkit.command.snoop.SnoopCommand
import com.rpkit.chat.bukkit.command.unmute.UnmuteCommand
import com.rpkit.chat.bukkit.database.table.RPKChatChannelMuteTable
import com.rpkit.chat.bukkit.database.table.RPKChatChannelSpeakerTable
import com.rpkit.chat.bukkit.database.table.RPKChatGroupInviteTable
import com.rpkit.chat.bukkit.database.table.RPKChatGroupMemberTable
import com.rpkit.chat.bukkit.database.table.RPKChatGroupTable
import com.rpkit.chat.bukkit.database.table.RPKLastUsedChatGroupTable
import com.rpkit.chat.bukkit.database.table.RPKSnooperTable
import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.chat.bukkit.discord.RPKDiscordServiceImpl
import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.chat.bukkit.irc.RPKIRCServiceImpl
import com.rpkit.chat.bukkit.listener.AsyncPlayerChatListener
import com.rpkit.chat.bukkit.listener.PlayerCommandPreprocessListener
import com.rpkit.chat.bukkit.messages.ChatMessages
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteService
import com.rpkit.chat.bukkit.prefix.RPKPrefixService
import com.rpkit.chat.bukkit.prefix.RPKPrefixServiceImpl
import com.rpkit.chat.bukkit.snooper.RPKSnooperService
import com.rpkit.chat.bukkit.snooper.RPKSnooperServiceImpl
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerService
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import java.io.File

/**
 * RPK chat plugin default implementation.
 */
class RPKChatBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: ChatMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.chat.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4383)
        // Directed pre-format pipeline components
        ConfigurationSerialization.registerClass(DrunkenSlurComponent::class.java, "DrunkenSlurComponent")
        ConfigurationSerialization.registerClass(GarbleComponent::class.java, "GarbleComponent")
        ConfigurationSerialization.registerClass(LanguageComponent::class.java, "LanguageComponent")
        ConfigurationSerialization.registerClass(RadiusFilterComponent::class.java, "RadiusFilterComponent")

        // Format parts
        ConfigurationSerialization.registerClass(ChannelPart::class.java, "ChannelPart")
        ConfigurationSerialization.registerClass(MessagePart::class.java, "MessagePart")
        ConfigurationSerialization.registerClass(ReceiverCharacterNamePart::class.java, "ReceiverCharacterNamePart")
        ConfigurationSerialization.registerClass(ReceiverPrefixPart::class.java, "ReceiverPrefixPart")
        ConfigurationSerialization.registerClass(ReceiverProfileNamePart::class.java, "ReceiverProfileNamePart")
        ConfigurationSerialization.registerClass(SenderCharacterNamePart::class.java, "SenderCharacterNamePart")
        ConfigurationSerialization.registerClass(SenderPrefixPart::class.java, "SenderPrefixPart")
        ConfigurationSerialization.registerClass(SenderProfileNamePart::class.java, "SenderProfileNamePart")
        ConfigurationSerialization.registerClass(TextPart::class.java)

        // Hover actions
        ConfigurationSerialization.registerClass(ShowTextHoverAction::class.java, "ShowTextHoverAction")

        // Click actions
        ConfigurationSerialization.registerClass(CopyToClipboardClickAction::class.java, "CopyToClipboardClickAction")
        ConfigurationSerialization.registerClass(OpenFileClickAction::class.java, "OpenFileClickAction")
        ConfigurationSerialization.registerClass(OpenURLClickAction::class.java, "OpenURLClickAction")
        ConfigurationSerialization.registerClass(RunCommandClickAction::class.java, "RunCommandClickAction")
        ConfigurationSerialization.registerClass(SuggestCommandClickAction::class.java, "SuggestCommandClickAction")

        //Directed post-format pipeline components
        ConfigurationSerialization.registerClass(SendMessageComponent::class.java, "SendMessageComponent")
        ConfigurationSerialization.registerClass(SnoopComponent::class.java, "SnoopComponent")

        // Undirected pipeline components
        ConfigurationSerialization.registerClass(DiscordComponent::class.java, "DiscordComponent")
        ConfigurationSerialization.registerClass(IRCComponent::class.java, "IRCComponent")
        ConfigurationSerialization.registerClass(LogComponent::class.java, "LogComponent")
        ConfigurationSerialization.registerClass(UndirectedFormatComponent::class.java, "UndirectedFormatComponent")
        ConfigurationSerialization.registerClass(WebComponent::class.java, "WebComponent")

        saveDefaultConfig()

        messages = ChatMessages(this)
        messages.saveDefaultMessagesConfig()

        val databaseConfigFile = File(dataFolder, "database.yml")
        if (!databaseConfigFile.exists()) {
            saveResource("database.yml", false)
        }
        val databaseConfig = YamlConfiguration.loadConfiguration(databaseConfigFile)
        val databaseUrl = databaseConfig.getString("database.url")
        if (databaseUrl == null) {
            logger.severe("Database URL not set!")
            isEnabled = false
            return
        }
        val databaseUsername = databaseConfig.getString("database.username")
        val databasePassword = databaseConfig.getString("database.password")
        val databaseSqlDialect = databaseConfig.getString("database.dialect")
        val databaseMaximumPoolSize = databaseConfig.getInt("database.maximum-pool-size", 3)
        val databaseMinimumIdle = databaseConfig.getInt("database.minimum-idle", 3)
        if (databaseSqlDialect == null) {
            logger.severe("Database SQL dialect not set!")
            isEnabled = false
            return
        }
        database = Database(
                DatabaseConnectionProperties(
                        databaseUrl,
                        databaseUsername,
                        databasePassword,
                        databaseSqlDialect,
                        databaseMaximumPoolSize,
                        databaseMinimumIdle
                ),
                DatabaseMigrationProperties(
                        when (databaseSqlDialect) {
                            "MYSQL" -> "com/rpkit/chat/migrations/mysql"
                            "SQLITE" -> "com/rpkit/chat/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_chat"
                ),
                classLoader
        )
        database.addTable(RPKChatChannelMuteTable(database, this))
        database.addTable(RPKChatChannelSpeakerTable(database, this))
        database.addTable(RPKChatGroupTable(database, this))
        database.addTable(RPKChatGroupInviteTable(database))
        database.addTable(RPKChatGroupMemberTable(database, this))
        database.addTable(RPKLastUsedChatGroupTable(database, this))
        database.addTable(RPKSnooperTable(database, this))

        if (config.getBoolean("irc.enabled")) {
            Services[RPKIRCService::class.java] = RPKIRCServiceImpl(this)
        }

        if (config.getBoolean("discord.enabled")) {
            Services[RPKDiscordService::class.java] = RPKDiscordServiceImpl(this)
        }

        val prefixService = RPKPrefixServiceImpl(this)
        Services[RPKPrefixService::class.java] = prefixService
        val chatChannelService = RPKChatChannelServiceImpl(this)
        Services[RPKChatChannelService::class.java] = chatChannelService
        Services[RPKChatChannelMuteService::class.java] = RPKChatChannelMuteService(this)
        Services[RPKChatChannelSpeakerService::class.java] = RPKChatChannelSpeakerService(this)
        Services[RPKChatGroupService::class.java] = RPKChatGroupServiceImpl(this)
        Services[RPKSnooperService::class.java] = RPKSnooperServiceImpl(this)

        registerChatChannelPermissions(chatChannelService)
        registerPrefixPermissions(prefixService)

        registerCommands()
        registerListeners()
    }

    override fun onDisable() {
        if (config.getBoolean("irc.enabled")) {
            Services[RPKIRCService::class.java]?.disconnect()
        }
    }

    fun registerCommands() {
        getCommand("chatchannel")?.setExecutor(ChatChannelCommand(this))
        getCommand("mute")?.setExecutor(MuteCommand(this))
        getCommand("unmute")?.setExecutor(UnmuteCommand(this))
        getCommand("listchatchannels")?.setExecutor(ListChatChannelsCommand(this))
        getCommand("chatgroup")?.setExecutor(ChatGroupCommand(this))
        getCommand("message")?.setExecutor(MessageCommand(this))
        getCommand("reply")?.setExecutor(ReplyCommand(this))
        getCommand("snoop")?.setExecutor(SnoopCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                AsyncPlayerChatListener(this),
                PlayerCommandPreprocessListener(this)
        )
    }

    private fun registerChatChannelPermissions(chatChannelService: RPKChatChannelService) {
        chatChannelService.chatChannels.forEach { chatChannel ->
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

    private fun registerPrefixPermissions(prefixService: RPKPrefixService) {
        prefixService.prefixes.forEach { prefix ->
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.prefix.${prefix.name}",
                    "Gives the player the prefix ${prefix.name}",
                    PermissionDefault.FALSE
            ))
        }
    }

}