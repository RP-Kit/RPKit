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

package com.rpkit.statbuilds.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.skills.RPKSkillPointService
import com.rpkit.statbuilds.bukkit.command.statattribute.StatAttributeCommand
import com.rpkit.statbuilds.bukkit.command.statbuild.StatBuildCommand
import com.rpkit.statbuilds.bukkit.database.table.RPKCharacterStatPointsTable
import com.rpkit.statbuilds.bukkit.listener.AsyncPlayerPreLoginListener
import com.rpkit.statbuilds.bukkit.listener.PlayerQuitListener
import com.rpkit.statbuilds.bukkit.listener.RPKCharacterDeleteListener
import com.rpkit.statbuilds.bukkit.listener.RPKCharacterSwitchListener
import com.rpkit.statbuilds.bukkit.messages.StatBuildsMessages
import com.rpkit.statbuilds.bukkit.skillpoint.RPKSkillPointServiceImpl
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeService
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeServiceImpl
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildServiceImpl
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableName
import com.rpkit.stats.bukkit.stat.RPKStatVariableService
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class RPKStatBuildsBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: StatBuildsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.statbuilds.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.statbuilds.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 6663)
        saveDefaultConfig()

        messages = StatBuildsMessages(this)

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
                            "MYSQL" -> "com/rpkit/statbuilds/migrations/mysql"
                            "SQLITE" -> "com/rpkit/statbuilds/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_stat_builds"
                ),
                classLoader
        )
        database.addTable(RPKCharacterStatPointsTable(database, this))

        val statAttributeService = RPKStatAttributeServiceImpl(this)
        Services[RPKStatAttributeService::class.java] = statAttributeService
        val statBuildService = RPKStatBuildServiceImpl(this)
        Services[RPKStatBuildService::class.java] = statBuildService
        Services[RPKSkillPointService::class.java] = RPKSkillPointServiceImpl(this)

        Services.require(RPKStatVariableService::class.java).whenAvailable { statVariableService ->
            statAttributeService.statAttributes.forEach { statAttribute ->
                statVariableService.addStatVariable(object : RPKStatVariable {
                    override val name = RPKStatVariableName(statAttribute.name.value)
                    override fun get(character: RPKCharacter) =
                            statBuildService.getPreloadedStatPoints(character, statAttribute)?.toDouble() ?: 0.0
                })
            }
        }

        registerCommands()
        registerListeners()
    }

    private fun registerCommands() {
        getCommand("statbuild")?.setExecutor(StatBuildCommand(this))
        getCommand("statattribute")?.setExecutor(StatAttributeCommand(this))
    }

    private fun registerListeners() {
        registerListeners(
            AsyncPlayerPreLoginListener(),
            PlayerQuitListener(),
            RPKCharacterSwitchListener(),
            RPKCharacterDeleteListener(this)
        )
    }

}