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

package com.rpkit.banks.bukkit

import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.banks.bukkit.bank.RPKBankServiceImpl
import com.rpkit.banks.bukkit.database.table.RPKBankTable
import com.rpkit.banks.bukkit.listener.PlayerInteractListener
import com.rpkit.banks.bukkit.listener.SignChangeListener
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * RPK banks plugin default implementation.
 */
class RPKBanksBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        Metrics(this, 4378)
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
                            "MYSQL" -> "com/rpkit/banks/migrations/mysql"
                            "SQLITE" -> "com/rpkit/banks/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_banks"
                ),
                classLoader
        )
        database.addTable(RPKBankTable(database, this))

        Services[RPKBankService::class] = RPKBankServiceImpl(this)
    }

    override fun registerListeners() {
        registerListeners(SignChangeListener(this), PlayerInteractListener(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("bank-sign-invalid-operation", "&cThat's not a valid operation. Please use withdraw, deposit, or balance. (Line 2)")
        messages.setDefault("bank-sign-invalid-currency", "&cThat's not a valid currency. Please use a valid currency. (Line 4)")
        messages.setDefault("bank-withdraw-invalid-wallet-full", "&cYou cannot withdraw that amount, it would not fit in your wallet.")
        messages.setDefault("bank-withdraw-invalid-not-enough-money", "&cYou cannot withdraw that amount, your bank balance is not high enough.")
        messages.setDefault("bank-withdraw-valid", "&aWithdrew \$amount \$currency. Wallet balance: \$wallet-balance. Bank balance: \$bank-balance.")
        messages.setDefault("bank-deposit-invalid-not-enough-money", "&cYou cannot deposit that amount, your wallet balance is not high enough.")
        messages.setDefault("bank-deposit-valid", "&aDeposited \$amount \$currency. Wallet balance: \$wallet-balance. Bank balance: \$bank-balance.")
        messages.setDefault("bank-balance-valid", "&aBalance: \$amount \$currency")
        messages.setDefault("no-permission-bank-create", "&cYou do not have permission to create banks.")
        messages.setDefault("no-currency-service", "&cThere is no currency service available.")
        messages.setDefault("no-bank-service", "&cThere is no bank service available.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-economy-service", "&cThere is no economy service available.")
    }

}