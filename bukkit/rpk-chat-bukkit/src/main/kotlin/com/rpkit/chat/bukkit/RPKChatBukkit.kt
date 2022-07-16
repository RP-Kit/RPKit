/*
 * Copyright 2022 Ren Binden
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
import com.rpkit.chat.bukkit.chatchannel.format.click.OpenFileClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.OpenURLClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.RunCommandClickAction
import com.rpkit.chat.bukkit.chatchannel.format.click.SuggestCommandClickAction
import com.rpkit.chat.bukkit.chatchannel.format.hover.ShowTextHoverAction
import com.rpkit.chat.bukkit.chatchannel.format.part.*
import com.rpkit.chat.bukkit.chatchannel.undirected.*
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
import com.rpkit.chat.bukkit.database.table.*
import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.chat.bukkit.discord.RPKDiscordServiceImpl
import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.chat.bukkit.irc.RPKIRCServiceImpl
import com.rpkit.chat.bukkit.listener.AsyncPlayerChatListener
import com.rpkit.chat.bukkit.listener.PlayerCommandPreprocessListener
import com.rpkit.chat.bukkit.listener.RPKMinecraftProfileDeleteListener
import com.rpkit.chat.bukkit.messages.ChatMessages
import com.rpkit.chat.bukkit.mute.RPKChatChannelMuteService
import com.rpkit.chat.bukkit.prefix.RPKPrefixService
import com.rpkit.chat.bukkit.prefix.RPKPrefixServiceImpl
import com.rpkit.chat.bukkit.snooper.RPKSnooperService
import com.rpkit.chat.bukkit.snooper.RPKSnooperServiceImpl
import com.rpkit.chat.bukkit.speaker.RPKChatChannelSpeakerService
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * RPK chat plugin default implementation.
 */
class RPKChatBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: ChatMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.chat.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.chat.bukkit.shadow.impl.org.jooq.no-tips", "true")

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
        database.addTable(RPKChatGroupInviteTable(database, this))
        database.addTable(RPKChatGroupMemberTable(database, this))
        database.addTable(RPKLastUsedChatGroupTable(database, this))
        database.addTable(RPKSnooperTable(database, this))

        // Class loader needs to be the plugin's class loader in order for ServiceLoader in slf4j to be able to find
        // the SLF4JLoggerProvider implementation, as the remapped slf4j-jdk14 is loaded by the plugin's class loader
        // and isn't accessible outside of that for whatever reason.
        // Both JDA and PircBotX make use of slf4j for logging, so load the IRC and Discord services with the plugin
        // class loader instead of the server's, before switching back to the normal class loader.
        val oldClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
        if (config.getBoolean("irc.enabled")) {
            Services[RPKIRCService::class.java] = RPKIRCServiceImpl(this)
        }

        if (config.getBoolean("discord.enabled")) {
            Services[RPKDiscordService::class.java] = RPKDiscordServiceImpl(this)
        }
        Thread.currentThread().contextClassLoader = oldClassLoader

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
        if (config.getBoolean("discord.enabled")) {
            Services[RPKDiscordService::class.java]?.disconnect()
        }
    }

    private fun registerCommands() {
        getCommand("chatchannel")?.setExecutor(ChatChannelCommand(this))
        getCommand("mute")?.setExecutor(MuteCommand(this))
        getCommand("unmute")?.setExecutor(UnmuteCommand(this))
        getCommand("listchatchannels")?.setExecutor(ListChatChannelsCommand(this))
        getCommand("chatgroup")?.setExecutor(ChatGroupCommand(this))
        getCommand("message")?.setExecutor(MessageCommand(this))
        getCommand("reply")?.setExecutor(ReplyCommand(this))
        getCommand("snoop")?.setExecutor(SnoopCommand(this))
    }

    private fun registerListeners() {
        registerListeners(
            AsyncPlayerChatListener(this),
            PlayerCommandPreprocessListener(this),
            RPKMinecraftProfileDeleteListener(this)
        )
    }

    private fun registerChatChannelPermissions(chatChannelService: RPKChatChannelService) {
        chatChannelService.chatChannels.forEach { chatChannel ->
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.command.chatchannel.${chatChannel.name.value}",
                    "Allows speaking in ${chatChannel.name.value}",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.command.mute.${chatChannel.name.value}",
                    "Allows muting ${chatChannel.name.value}",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.command.unmute.${chatChannel.name.value}",
                    "Allows unmuting ${chatChannel.name.value}",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.chat.listen.${chatChannel.name.value}",
                    "Allows listening to ${chatChannel.name.value}",
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