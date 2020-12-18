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

package com.rpkit.classes.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.classes.bukkit.character.ClassField
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.classes.bukkit.classes.RPKClassServiceImpl
import com.rpkit.classes.bukkit.command.`class`.ClassCommand
import com.rpkit.classes.bukkit.database.table.RPKCharacterClassTable
import com.rpkit.classes.bukkit.database.table.RPKClassExperienceTable
import com.rpkit.classes.bukkit.messages.ClassesMessages
import com.rpkit.classes.bukkit.skillpoint.RPKSkillPointServiceImpl
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.skills.RPKSkillPointService
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableService
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKClassesBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: ClassesMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.classes.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4386)
        saveDefaultConfig()

        messages = ClassesMessages(this)
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
                            "MYSQL" -> "com/rpkit/classes/migrations/mysql"
                            "SQLITE" -> "com/rpkit/classes/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_classes"
                ),
                classLoader
        )
        database.addTable(RPKCharacterClassTable(database, this))
        database.addTable(RPKClassExperienceTable(database, this))

        Services[RPKClassService::class.java] = RPKClassServiceImpl(this)
        Services[RPKSkillPointService::class.java] = RPKSkillPointServiceImpl(this)

        Services.require(RPKStatVariableService::class.java).whenAvailable { statVariableService ->
            Services.require(RPKClassService::class.java).whenAvailable { classService ->
                config.getConfigurationSection("classes")
                        ?.getKeys(false)
                        ?.flatMap { className ->
                            config.getConfigurationSection("classes.$className.stat-variables")?.getKeys(false) ?: setOf()
                        }
                        ?.toSet()
                        ?.forEach { statVariableName ->
                            statVariableService.addStatVariable(object : RPKStatVariable {
                                override val name = statVariableName

                                override fun get(character: RPKCharacter): Double {
                                    val `class` = classService.getClass(character)
                                    return `class`?.getStatVariableValue(this, classService.getLevel(character, `class`))?.toDouble()
                                            ?: 0.0
                                }
                            })
                        }
            }
        }

        Services.require(RPKCharacterCardFieldService::class.java).whenAvailable { service ->
            service.characterCardFields.add(ClassField())
        }

        registerCommands()
    }

    fun registerCommands() {
        getCommand("class")?.setExecutor(ClassCommand(this))
    }

}