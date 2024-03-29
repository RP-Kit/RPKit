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

package com.rpkit.permissions.bukkit

import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.command.charactergroup.CharacterGroupCommand
import com.rpkit.permissions.bukkit.command.group.GroupCommand
import com.rpkit.permissions.bukkit.command.permissions.PermissionsCommand
import com.rpkit.permissions.bukkit.database.table.RPKCharacterGroupTable
import com.rpkit.permissions.bukkit.database.table.RPKProfileGroupTable
import com.rpkit.permissions.bukkit.group.RPKGroupImpl
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.RPKGroupServiceImpl
import com.rpkit.permissions.bukkit.listener.*
import com.rpkit.permissions.bukkit.messages.PermissionsMessages
import com.rpkit.permissions.bukkit.permissions.RPKPermissionsService
import com.rpkit.permissions.bukkit.permissions.RPKPermissionsServiceImpl
import com.rpkit.permissions.bukkit.placeholder.RPKPermissionsPlaceholderExpansion
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * RPK permissions plugin default implementation.
 */
class RPKPermissionsBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: PermissionsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.permissions.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.permissions.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4407)
        ConfigurationSerialization.registerClass(RPKGroupImpl::class.java, "RPKGroupImpl")
        saveDefaultConfig()
        config.options().pathSeparator('/')

        messages = PermissionsMessages(this)

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
                            "MYSQL" -> "com/rpkit/permissions/migrations/mysql"
                            "SQLITE" -> "com/rpkit/permissions/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_permissions"
                ),
                classLoader
        )
        database.addTable(RPKProfileGroupTable(database, this))
        database.addTable(RPKCharacterGroupTable(database, this))

        Services[RPKGroupService::class.java] = RPKGroupServiceImpl(this)
        Services[RPKPermissionsService::class.java] = RPKPermissionsServiceImpl(this)

        registerCommands()
        registerListeners()

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            RPKPermissionsPlaceholderExpansion(this).register()
        }
    }

    private fun registerCommands() {
        getCommand("group")?.setExecutor(GroupCommand(this))
        getCommand("charactergroup")?.setExecutor(CharacterGroupCommand(this))
        getCommand("permissions")?.setExecutor(PermissionsCommand(this).toBukkit())
    }

    private fun registerListeners() {
        registerListeners(
            PlayerJoinListener(),
            PlayerQuitListener(this),
            RPKCharacterSwitchListener(this),
            AsyncPlayerPreLoginListener(),
            RPKCharacterDeleteListener(this),
            RPKProfileDeleteListener(this)
        )
    }
}