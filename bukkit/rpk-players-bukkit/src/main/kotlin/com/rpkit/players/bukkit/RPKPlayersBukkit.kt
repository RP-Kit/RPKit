/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.players.bukkit

import com.rpkit.core.bukkit.command.sender.resolver.RPKBukkitCommandSenderResolutionService
import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.RPKBukkitMinecraftProfileCommandSenderResolver
import com.rpkit.players.bukkit.command.profile.ProfileCommand
import com.rpkit.players.bukkit.database.table.RPKDiscordProfileTable
import com.rpkit.players.bukkit.database.table.RPKGitHubProfileTable
import com.rpkit.players.bukkit.database.table.RPKIRCProfileTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileLinkRequestTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTable
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import com.rpkit.players.bukkit.listener.PlayerJoinListener
import com.rpkit.players.bukkit.listener.PlayerLoginListener
import com.rpkit.players.bukkit.messages.PlayersMessages
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileServiceImpl
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileServiceImpl
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileServiceImpl
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileServiceImpl
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKProfileServiceImpl
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileService
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileService
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.web.PlayersWebAPI
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.text.Charsets.UTF_8

/**
 * RPK players plugin default implementation.
 */
class RPKPlayersBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: PlayersMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.players.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4409)

        saveDefaultConfig()

        messages = PlayersMessages(this)

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
                            "MYSQL" -> "com/rpkit/players/migrations/mysql"
                            "SQLITE" -> "com/rpkit/players/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_players"
                ),
                classLoader
        )
        database.addTable(RPKDiscordProfileTable(database, this))
        database.addTable(RPKGitHubProfileTable(database, this))
        database.addTable(RPKIRCProfileTable(database, this))
        database.addTable(RPKMinecraftProfileTable(database, this))
        database.addTable(RPKMinecraftProfileLinkRequestTable(database))
        database.addTable(RPKProfileTable(database, this))

        Services[RPKDiscordProfileService::class.java] = RPKDiscordProfileServiceImpl(this)
        Services[RPKGitHubProfileService::class.java] = RPKGitHubProfileServiceImpl(this)
        Services[RPKIRCProfileService::class.java] = RPKIRCProfileServiceImpl(this)
        Services[RPKMinecraftProfileService::class.java] = RPKMinecraftProfileServiceImpl(this)
        Services[RPKProfileService::class.java] = RPKProfileServiceImpl(this)

        Services.require(RPKBukkitCommandSenderResolutionService::class.java).whenAvailable { commandSenderResolutionService ->
            commandSenderResolutionService.addResolver(RPKBukkitMinecraftProfileCommandSenderResolver())
        }

        registerCommands()
        registerListeners()

        saveDefaultWebConfig()
        if (getWebConfig().getBoolean("enabled")) {
            thread { PlayersWebAPI(this).start() }
        }
    }

    private fun registerCommands() {
        getCommand("profile")?.setExecutor(ProfileCommand(this).toBukkit())
    }

    private fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerLoginListener(this))
    }

    private var webConfig: FileConfiguration? = null
    private val webConfigFile = File(dataFolder, "web.yml")

    fun getWebConfig(): FileConfiguration {
        return webConfig ?: reloadWebConfig()
    }

    fun reloadWebConfig(): FileConfiguration = YamlConfiguration.loadConfiguration(webConfigFile)
        .also { config ->
            val defConfigStream = getResource("web.yml")
            if (defConfigStream != null) {
                config.setDefaults(
                    YamlConfiguration.loadConfiguration(
                        InputStreamReader(
                            defConfigStream,
                            UTF_8
                        )
                    )
                )
            }
            webConfig = config
        }

    fun saveWebConfig() {
        try {
            getWebConfig().save(webConfigFile)
        } catch (exception: IOException) {
            logger.log(Level.SEVERE, "Could not save config to $webConfigFile", exception)
        }
    }

    fun saveDefaultWebConfig() {
        if (!webConfigFile.exists()) {
            saveResource("web.yml", false)
        }
    }

}
