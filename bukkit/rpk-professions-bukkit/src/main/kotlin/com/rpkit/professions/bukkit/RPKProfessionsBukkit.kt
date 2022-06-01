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

package com.rpkit.professions.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.professions.bukkit.character.ProfessionField
import com.rpkit.professions.bukkit.command.profession.ProfessionCommand
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionChangeCooldownTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionExperienceTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionTable
import com.rpkit.professions.bukkit.database.table.RPKProfessionHiddenTable
import com.rpkit.professions.bukkit.listener.*
import com.rpkit.professions.bukkit.messages.ProfessionsMessages
import com.rpkit.professions.bukkit.placeholder.RPKProfessionsPlaceholderExpansion
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import com.rpkit.professions.bukkit.profession.RPKProfessionServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class RPKProfessionsBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: ProfessionsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.professions.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.professions.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 5352)
        saveDefaultConfig()

        messages = ProfessionsMessages(this)

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
                            "MYSQL" -> "com/rpkit/professions/migrations/mysql"
                            "SQLITE" -> "com/rpkit/professions/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_professions"
                ),
                classLoader
        )
        database.addTable(RPKCharacterProfessionChangeCooldownTable(database, this))
        database.addTable(RPKCharacterProfessionExperienceTable(database, this))
        database.addTable(RPKCharacterProfessionTable(database, this))
        database.addTable(RPKProfessionHiddenTable(database, this))

        Services[RPKProfessionService::class.java] = RPKProfessionServiceImpl(this)
        Services.require(RPKCharacterCardFieldService::class.java).whenAvailable { service ->
            service.characterCardFields.add(ProfessionField(this))
        }

        registerCommands()
        registerListeners()

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            RPKProfessionsPlaceholderExpansion(this).register()
        }
    }

    fun registerCommands() {
        getCommand("profession")?.setExecutor(ProfessionCommand(this))
    }

    fun registerListeners() {
        registerListeners(
            BlockBreakListener(this),
            CraftItemListener(this),
            InventoryClickListener(this),
            PrepareItemCraftListener(this),
            RPKBukkitCharacterDeleteListener(this),
            AsyncPlayerPreLoginListener(),
            PlayerQuitListener(),
            RPKCharacterSwitchListener()
        )
    }

}