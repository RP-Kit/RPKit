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

package com.rpkit.languages.bukkit

import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguageService
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguageServiceImpl
import com.rpkit.languages.bukkit.command.LanguageCommand
import com.rpkit.languages.bukkit.database.table.RPKCharacterLanguageTable
import com.rpkit.languages.bukkit.language.RPKLanguageService
import com.rpkit.languages.bukkit.language.RPKLanguageServiceImpl
import com.rpkit.languages.bukkit.listener.RPKCharacterDeleteListener
import com.rpkit.languages.bukkit.messages.LanguageMessages
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class RPKLanguagesBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: LanguageMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.languages.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.languages.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 6764)
        saveDefaultConfig()
        var configUpdated = false
        for (languageName in config.getConfigurationSection("languages")?.getKeys(false) ?: emptySet()) {
            if (config.contains("languages.${languageName}.default-race-understanding")) {
                config.set("languages.${languageName}.default-species-understanding", config.get("languages.${languageName}.default-race-understanding"))
                config.set("languages.${languageName}.default-race-understanding", null)
                configUpdated = true
            }
        }
        if (configUpdated) {
            saveConfig()
        }

        messages = LanguageMessages(this)

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
                    "MYSQL" -> "com/rpkit/languages/migrations/mysql"
                    "SQLITE" -> "com/rpkit/languages/migrations/sqlite"
                    else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                },
                "flyway_schema_history_languages"
            ),
            classLoader
        )
        database.addTable(RPKCharacterLanguageTable(database, this))

        Services[RPKLanguageService::class.java] = RPKLanguageServiceImpl(this)
        Services[RPKCharacterLanguageService::class.java] = RPKCharacterLanguageServiceImpl(this)

        registerListeners()
        registerCommands()
    }

    private fun registerListeners() {
        registerListeners(RPKCharacterDeleteListener(this))
    }

    private fun registerCommands() {
        getCommand("language")?.setExecutor(LanguageCommand(this).toBukkit())
    }

}
