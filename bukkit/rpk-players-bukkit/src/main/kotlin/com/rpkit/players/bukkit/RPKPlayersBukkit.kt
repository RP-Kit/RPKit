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

package com.rpkit.players.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.account.AccountCommand
import com.rpkit.players.bukkit.command.profile.ProfileCommand
import com.rpkit.players.bukkit.database.table.RPKDiscordProfileTable
import com.rpkit.players.bukkit.database.table.RPKGitHubProfileTable
import com.rpkit.players.bukkit.database.table.RPKIRCProfileTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileLinkRequestTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTable
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import com.rpkit.players.bukkit.listener.PlayerJoinListener
import com.rpkit.players.bukkit.listener.PlayerLoginListener
import com.rpkit.players.bukkit.profile.RPKDiscordProfileService
import com.rpkit.players.bukkit.profile.RPKDiscordProfileServiceImpl
import com.rpkit.players.bukkit.profile.RPKGitHubProfileService
import com.rpkit.players.bukkit.profile.RPKGitHubProfileServiceImpl
import com.rpkit.players.bukkit.profile.RPKIRCProfileService
import com.rpkit.players.bukkit.profile.RPKIRCProfileServiceImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileServiceImpl
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKProfileServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * RPK players plugin default implementation.
 */
class RPKPlayersBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        Metrics(this, 4409)

        saveDefaultConfig()

        Services[RPKDiscordProfileService::class] = RPKDiscordProfileServiceImpl(this)
        Services[RPKGitHubProfileService::class] = RPKGitHubProfileServiceImpl(this)
        Services[RPKIRCProfileService::class] = RPKIRCProfileServiceImpl(this)
        Services[RPKMinecraftProfileService::class] = RPKMinecraftProfileServiceImpl(this)
        Services[RPKProfileService::class] = RPKProfileServiceImpl(this)

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
    }

    override fun registerCommands() {
        getCommand("account")?.setExecutor(AccountCommand(this))
        getCommand("profile")?.setExecutor(ProfileCommand(this))
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerLoginListener())
    }

    override fun setDefaultMessages() {
        messages.setDefault("account-usage", "&cUsage: /account [link|confirmlink|denylink]")
        messages.setDefault("account-link-usage", "&cUsage: /account link [irc|minecraft|discord]")
        messages.setDefault("account-link-discord-invalid-user-tag", "&cThat is not a valid discord tag.")
        messages.setDefault("account-link-discord-invalid-user", "&cThat is not a valid discord user.")
        messages.setDefault("account-link-irc-usage", "&cUsage: /account link irc [nick]")
        messages.setDefault("account-link-irc-invalid-already-linked", "&cThat IRC user is already linked to a Minecraft user.")
        messages.setDefault("account-link-irc-invalid-nick", "&cThere is no IRC user by that name online.")
        messages.setDefault("account-link-irc-invalid-no-irc-service", "&cThere is no IRC service registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-invalid-no-player-service", "&cThere is no player service registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-valid", "&aAccount linked.")
        messages.setDefault("account-link-minecraft-usage", "&cUsage: /account link minecraft [name] [token]")
        messages.setDefault("account-link-minecraft-invalid-player", "&cThere is no player by that name.")
        messages.setDefault("account-link-minecraft-invalid-minecraft-profile", "&cThat account has no Minecraft profile. Please get them to relog, or contact the server owner if this error persists.")
        messages.setDefault("account-link-minecraft-invalid-token", "&cThat token is invalid. Please use the token exactly as provided on your other account.")
        messages.setDefault("account-link-minecraft-valid", "&aAccount link request placed. Please log in to your other account and accept the request.")
        messages.setDefault("account-confirm-link-usage", "&cUsage: /account confirmlink [type] [id]")
        messages.setDefault("account-confirm-link-invalid-id", "&cInvalid ID.")
        messages.setDefault("account-confirm-link-invalid-already-linked", "&cYour Minecraft profile is already linked to a profile. You may not link to more than one profile.")
        messages.setDefault("account-confirm-link-invalid-request", "&cThat profile has not requested to link this Minecraft profile.")
        messages.setDefault("account-confirm-link-valid", "&aAccount linked.")
        messages.setDefault("account-confirm-link-invalid-type", "&cInvalid account type.")
        messages.setDefault("account-deny-link-usage", "&cUsage: /account denylink [type] [id]")
        messages.setDefault("account-deny-link-invalid-id", "&cInvalid ID.")
        messages.setDefault("account-deny-link-invalid-request", "&cThat profile has not requested to link this Minecraft profile.")
        messages.setDefault("account-deny-link-valid", "&aLink request denied.")
        messages.setDefault("account-deny-link-profile-created", "&aThere are no outstanding link requests for this account, so a new profile has been created for it.")
        messages.setDefault("account-deny-link-invalid-type", "&cInvalid account type.")
        messages.setDefault("profile-name-usage", "&cUsage: /profile name [name]")
        messages.setDefault("profile-name-invalid-name", "&cName must be between 3 and 16 characters and contain alphanumerics and underscores only.")
        messages.setDefault("profile-name-valid", "&aProfile name set to \$name.")
        messages.setDefault("profile-password-usage", "&cUsage: /profile password [password]")
        messages.setDefault("profile-password-valid", "&aProfile password set.")
        messages.setDefault("profile-usage", "&cUsage: /profile [name|password]")
        messages.setDefault("no-profile-self", "&cA profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-minecraft-profile-self", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("profile-link-request", "&fWould you like to link this Minecraft account to \$profile?")
        messages.setDefault("yes", "Yes")
        messages.setDefault("no", "No")
        messages.setDefault("no-permission-account-link", "&cYou do not have permission to link accounts.")
        messages.setDefault("no-permission-account-link-discord", "&cYou do not have permission to link Discord accounts.")
        messages.setDefault("no-permission-account-link-irc", "&cYou do not have permission to link IRC accounts.")
        messages.setDefault("no-permission-account-link-minecraft", "&cYou do not have permission to link Minecraft accounts.")
        messages.setDefault("no-permission-profile-create", "&cYou do not have permission to create profiles.")
        messages.setDefault("no-permission-profile-login", "&cYou do not have permission to login to profiles.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-irc-profile-service", "&cThere is not IRC profile service available.")
        messages.setDefault("no-profile-service", "&cThere is no profile service available.")
        messages.setDefault("no-discord-service", "&cThere is no Discord service available.")
    }

}
