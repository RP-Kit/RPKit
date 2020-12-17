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

package com.rpkit.experience.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.character.ExperienceField
import com.rpkit.experience.bukkit.character.LevelField
import com.rpkit.experience.bukkit.command.experience.ExperienceCommand
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.experience.bukkit.experience.RPKExperienceServiceImpl
import com.rpkit.experience.bukkit.listener.PlayerExpChangeListener
import com.rpkit.experience.bukkit.listener.PlayerJoinListener
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKExperienceBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        System.setProperty("com.rpkit.experience.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4393)
        saveDefaultConfig()

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
            service.characterCardFields.add(ExperienceField(this))
            service.characterCardFields.add(LevelField(this))
        }
    }

    override fun registerListeners() {
        registerListeners(PlayerExpChangeListener(), PlayerJoinListener())
    }

    override fun registerCommands() {
        getCommand("experience")?.setExecutor(ExperienceCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("experience-usage", "&cUsage: /experience [add|set]")
        messages.setDefault("experience-set-usage", "&cUsage: /experience set [player] [value]")
        messages.setDefault("experience-set-experience-invalid-number", "&cYou must specify a number for the amount of experience to set.")
        messages.setDefault("experience-set-player-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("experience-set-valid", "&aExperience set.")
        messages.setDefault("experience-setlevel-usage", "&cUsage: /experience setlevel [player] [value]")
        messages.setDefault("experience-setlevel-level-invalid-number", "&cYou must specify a number for the level to set.")
        messages.setDefault("experience-setlevel-player-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("experience-setlevel-valid", "&aLevel set.")
        messages.setDefault("experience-add-usage", "&cUsage: /experience add [player] [value]")
        messages.setDefault("experience-add-experience-invalid-number", "&cYou must specify a number for the amount of experience to add.")
        messages.setDefault("experience-add-experience-invalid-negative", "&cYou may not add negative experience.")
        messages.setDefault("experience-add-player-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("experience-add-valid", "&aExperience added.")
        messages.setDefault("no-character-other", "&cThat player does not currently have a character.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-permission-experience-set", "&cYou do not have permission to set experience.")
        messages.setDefault("no-permission-experience-setlevel", "&cYou do not have permission to set level.")
        messages.setDefault("no-permission-experience-add", "&cYou do not have permission to add experience.")
        messages.setDefault("no-experience-service", "&cThere is no experience service available.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
    }

}