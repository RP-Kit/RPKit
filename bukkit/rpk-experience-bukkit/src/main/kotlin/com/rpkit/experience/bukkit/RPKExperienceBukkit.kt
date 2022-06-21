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

package com.rpkit.experience.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.character.ExperienceField
import com.rpkit.experience.bukkit.character.LevelField
import com.rpkit.experience.bukkit.command.experience.ExperienceCommand
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.experience.bukkit.experience.RPKExperienceServiceImpl
import com.rpkit.experience.bukkit.listener.*
import com.rpkit.experience.bukkit.messages.ExperienceMessages
import com.rpkit.experience.bukkit.placeholder.RPKExperiencePlaceholderExpansion
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class RPKExperienceBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: ExperienceMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.experience.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.experience.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4393)
        saveDefaultConfig()

        messages = ExperienceMessages(this)

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
                            "MYSQL" -> "com/rpkit/experience/migrations/mysql"
                            "SQLITE" -> "com/rpkit/experience/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_experience"
                ),
                classLoader
        )
        database.addTable(RPKExperienceTable(database, this))

        Services[RPKExperienceService::class.java] = RPKExperienceServiceImpl(this)

        Services.require(RPKCharacterCardFieldService::class.java).whenAvailable { service ->
            service.addCharacterCardField(ExperienceField(this))
            service.addCharacterCardField(LevelField(this))
        }

        registerListeners()
        registerCommands()

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            RPKExperiencePlaceholderExpansion(this).register()
        }
    }

    private fun registerListeners() {
        registerListeners(
            PlayerExpChangeListener(),
            PlayerJoinListener(this),
            AsyncPlayerPreLoginListener(),
            PlayerQuitListener(),
            RPKCharacterSwitchListener(this),
            RPKCharacterDeleteListener(this)
        )
    }

    private fun registerCommands() {
        getCommand("experience")?.setExecutor(ExperienceCommand(this))
    }

}