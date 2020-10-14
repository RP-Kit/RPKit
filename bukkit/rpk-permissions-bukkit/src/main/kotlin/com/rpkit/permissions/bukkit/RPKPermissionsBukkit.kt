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

package com.rpkit.permissions.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.command.charactergroup.CharacterGroupCommand
import com.rpkit.permissions.bukkit.command.group.GroupCommand
import com.rpkit.permissions.bukkit.database.table.RPKCharacterGroupTable
import com.rpkit.permissions.bukkit.database.table.RPKProfileGroupTable
import com.rpkit.permissions.bukkit.group.RPKGroupImpl
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.RPKGroupServiceImpl
import com.rpkit.permissions.bukkit.listener.PlayerJoinListener
import com.rpkit.permissions.bukkit.listener.PlayerQuitListener
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import java.io.File

/**
 * RPK permissions plugin default implementation.
 */
class RPKPermissionsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        Metrics(this, 4407)
        ConfigurationSerialization.registerClass(RPKGroupImpl::class.java, "RPKGroupImpl")
        saveDefaultConfig()
        config.options().pathSeparator('/')

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

        val groupService = RPKGroupServiceImpl(this)
        Services[RPKGroupService::class] = RPKGroupServiceImpl(this)
        groupService.groups.forEach { group ->
            server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.add.${group.name}",
                    "Allows adding the ${group.name} group to players",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.remove.${group.name}",
                    "Allows removing the ${group.name} group from players",
                    PermissionDefault.OP
            ))
        }
    }

    override fun registerCommands() {
        getCommand("group")?.setExecutor(GroupCommand(this))
        getCommand("charactergroup")?.setExecutor(CharacterGroupCommand(this))
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(), PlayerQuitListener())
    }

    override fun setDefaultMessages() {
        messages.setDefault("group-usage", "&cUsage: /group [add|remove|list]")
        messages.setDefault("group-add-valid", "&aGroup \$group added to \$player.")
        messages.setDefault("group-add-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("group-add-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("group-add-usage", "&cUsage: /group add [player] [group]")
        messages.setDefault("group-remove-valid", "&aGroup \$group removed from \$player.")
        messages.setDefault("group-remove-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("group-remove-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("group-remove-usage", "&cUsage: /group remove [player] [group]")
        messages.setDefault("group-list-title", "&fGroups:")
        messages.setDefault("group-list-item", "&7- \$group")
        messages.setDefault("character-group-usage", "&cUsage: /charactergroup [add|remove]")
        messages.setDefault("character-group-add-valid", "&aGroup \$group added to \$character.")
        messages.setDefault("character-group-add-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("character-group-add-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("character-group-add-usage", "&cUsage: /charactergroup add [player] [group]")
        messages.setDefault("character-group-remove-valid", "&aGroup \$group removed from \$character.")
        messages.setDefault("character-group-remove-invalid-group", "&cNo group by that name exists.")
        messages.setDefault("character-group-remove-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("character-group-remove-usage", "&cUsage: /charactergroup remove [player] [group]")
        messages.setDefault("no-profile", "&cYour Minecraft profile is not linked to a profile. Please link it on the server's web UI.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-character", "&cThat player must have a character to perform this action.")
        messages.setDefault("no-permission-group-add", "&cYou do not have permission to add groups to players.")
        messages.setDefault("no-permission-group-remove", "&cYou do not have permission to remove groups from players.")
        messages.setDefault("no-permission-group-add-group", "&cYou do not have permission to add \$group to players.")
        messages.setDefault("no-permission-group-remove-group", "&cYou do not have permission to remove \$group from players.")
        messages.setDefault("no-permission-group-list", "&cYou do not have permission to list groups.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-group-service", "&cThere is no group service available.")
    }
}