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

package com.rpkit.banks.bukkit

import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.banks.bukkit.bank.RPKBankServiceImpl
import com.rpkit.banks.bukkit.database.table.RPKBankTable
import com.rpkit.banks.bukkit.listener.PlayerInteractListener
import com.rpkit.banks.bukkit.listener.SignChangeListener
import com.rpkit.banks.bukkit.messages.BanksMessages
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * RPK banks plugin default implementation.
 */
class RPKBanksBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: BanksMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.banks.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.banks.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4378)
        saveDefaultConfig()

        messages = BanksMessages(this)

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
                            "MYSQL" -> "com/rpkit/banks/migrations/mysql"
                            "SQLITE" -> "com/rpkit/banks/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_banks"
                ),
                classLoader
        )
        database.addTable(RPKBankTable(database, this))

        Services[RPKBankService::class.java] = RPKBankServiceImpl(this)

        registerListeners()
    }

    fun registerListeners() {
        registerListeners(SignChangeListener(this), PlayerInteractListener(this))
    }

}