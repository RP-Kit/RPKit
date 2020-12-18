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

package com.rpkit.skills.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.command.BindSkillCommand
import com.rpkit.skills.bukkit.command.SkillCommand
import com.rpkit.skills.bukkit.command.UnbindSkillCommand
import com.rpkit.skills.bukkit.database.table.RPKSkillBindingTable
import com.rpkit.skills.bukkit.database.table.RPKSkillCooldownTable
import com.rpkit.skills.bukkit.listener.PlayerInteractListener
import com.rpkit.skills.bukkit.messages.SkillsMessages
import com.rpkit.skills.bukkit.skills.RPKSkillService
import com.rpkit.skills.bukkit.skills.RPKSkillServiceImpl
import com.rpkit.skills.bukkit.skills.RPKSkillTypeService
import com.rpkit.skills.bukkit.skills.RPKSkillTypeServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKSkillsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: SkillsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.skills.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4417)
        saveDefaultConfig()

        messages = SkillsMessages(this)
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
                            "MYSQL" -> "com/rpkit/skills/migrations/mysql"
                            "SQLITE" -> "com/rpkit/skills/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_skills"
                ),
                classLoader
        )

        database.addTable(RPKSkillCooldownTable(database, this))
        database.addTable(RPKSkillBindingTable(database, this))

        Services[RPKSkillTypeService::class.java] = RPKSkillTypeServiceImpl(this)
        Services[RPKSkillService::class.java] = RPKSkillServiceImpl(this)

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("skill")?.setExecutor(SkillCommand(this))
        getCommand("bindskill")?.setExecutor(BindSkillCommand(this))
        getCommand("unbindskill")?.setExecutor(UnbindSkillCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                PlayerInteractListener(this)
        )
    }

}