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

package com.rpkit.moderation.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.command.amivanished.AmIVanishedCommand
import com.rpkit.moderation.bukkit.command.onlinestaff.OnlineStaffCommand
import com.rpkit.moderation.bukkit.command.ticket.TicketCommand
import com.rpkit.moderation.bukkit.command.vanish.UnvanishCommand
import com.rpkit.moderation.bukkit.command.vanish.VanishCommand
import com.rpkit.moderation.bukkit.command.warn.WarningCommand
import com.rpkit.moderation.bukkit.command.warn.WarningCreateCommand
import com.rpkit.moderation.bukkit.command.warn.WarningRemoveCommand
import com.rpkit.moderation.bukkit.database.table.RPKTicketTable
import com.rpkit.moderation.bukkit.database.table.RPKVanishStateTable
import com.rpkit.moderation.bukkit.database.table.RPKWarningTable
import com.rpkit.moderation.bukkit.listener.PlayerJoinListener
import com.rpkit.moderation.bukkit.messages.ModerationMessages
import com.rpkit.moderation.bukkit.ticket.RPKTicketService
import com.rpkit.moderation.bukkit.ticket.RPKTicketServiceImpl
import com.rpkit.moderation.bukkit.vanish.RPKVanishService
import com.rpkit.moderation.bukkit.vanish.RPKVanishServiceImpl
import com.rpkit.moderation.bukkit.warning.RPKWarningService
import com.rpkit.moderation.bukkit.warning.RPKWarningServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKModerationBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: ModerationMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.moderation.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4403)

        saveDefaultConfig()

        messages = ModerationMessages(this)

        Services[RPKTicketService::class.java] = RPKTicketServiceImpl(this)
        Services[RPKVanishService::class.java] = RPKVanishServiceImpl(this)
        Services[RPKWarningService::class.java] = RPKWarningServiceImpl(this)

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
                            "MYSQL" -> "com/rpkit/moderation/migrations/mysql"
                            "SQLITE" -> "com/rpkit/moderation/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_players"
                ),
                classLoader
        )
        database.addTable(RPKTicketTable(database, this))
        database.addTable(RPKVanishStateTable(database, this))
        database.addTable(RPKWarningTable(database, this))

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("amivanished")?.setExecutor(AmIVanishedCommand(this))
        getCommand("onlinestaff")?.setExecutor(OnlineStaffCommand(this))
        getCommand("ticket")?.setExecutor(TicketCommand(this))
        getCommand("warning")?.setExecutor(WarningCommand(this))
        getCommand("warn")?.setExecutor(WarningCreateCommand(this))
        getCommand("unwarn")?.setExecutor(WarningRemoveCommand(this))
        getCommand("vanish")?.setExecutor(VanishCommand(this))
        getCommand("unvanish")?.setExecutor(UnvanishCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                PlayerJoinListener(this)
        )
    }

}