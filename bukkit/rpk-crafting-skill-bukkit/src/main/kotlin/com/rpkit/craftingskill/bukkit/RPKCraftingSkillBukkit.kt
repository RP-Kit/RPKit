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

package com.rpkit.craftingskill.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.craftingskill.bukkit.command.craftingskill.CraftingSkillCommand
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillService
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillServiceImpl
import com.rpkit.craftingskill.bukkit.database.table.RPKCraftingExperienceTable
import com.rpkit.craftingskill.bukkit.listener.*
import com.rpkit.craftingskill.bukkit.messages.CraftingSkillMessages
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKCraftingSkillBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: CraftingSkillMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.craftingskill.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 5350)
        saveDefaultConfig()

        messages = CraftingSkillMessages(this)

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
                            "MYSQL" -> "com/rpkit/craftingskill/migrations/mysql"
                            "SQLITE" -> "com/rpkit/craftingskill/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_crafting_skill"
                ),
                classLoader
        )
        database.addTable(RPKCraftingExperienceTable(database, this))

        Services[RPKCraftingSkillService::class.java] = RPKCraftingSkillServiceImpl(this)

        registerListeners()
        registerCommands()
    }

    private fun registerListeners() {
        registerListeners(
            RPKBukkitCharacterDeleteListener(this),
            BlockBreakListener(this),
            CraftItemListener(this),
            PrepareItemCraftListener(this),
            InventoryClickListener(this),
            AsyncPlayerPreLoginListener(this),
            PlayerQuitListener(this)
        )
    }

    private fun registerCommands() {
        getCommand("craftingskill")?.setExecutor(CraftingSkillCommand(this))
    }

}