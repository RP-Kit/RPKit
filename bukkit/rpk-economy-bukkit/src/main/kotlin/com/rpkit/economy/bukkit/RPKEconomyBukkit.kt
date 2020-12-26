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

package com.rpkit.economy.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.character.MoneyField
import com.rpkit.economy.bukkit.command.currency.CurrencyCommand
import com.rpkit.economy.bukkit.command.money.MoneyCommand
import com.rpkit.economy.bukkit.command.money.MoneyPayCommand
import com.rpkit.economy.bukkit.command.money.MoneyWalletCommand
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.currency.RPKCurrencyServiceImpl
import com.rpkit.economy.bukkit.database.table.RPKCurrencyTable
import com.rpkit.economy.bukkit.database.table.RPKMoneyHiddenTable
import com.rpkit.economy.bukkit.database.table.RPKWalletTable
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.economy.bukkit.economy.RPKEconomyServiceImpl
import com.rpkit.economy.bukkit.listener.InventoryClickListener
import com.rpkit.economy.bukkit.listener.InventoryCloseListener
import com.rpkit.economy.bukkit.listener.PlayerInteractListener
import com.rpkit.economy.bukkit.listener.SignChangeListener
import com.rpkit.economy.bukkit.messages.EconomyMessages
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * RPK economy plugin default implementation.
 */
class RPKEconomyBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: EconomyMessages

    private lateinit var currencyService: RPKCurrencyService
    private lateinit var economyService: RPKEconomyService

    override fun onEnable() {
        System.setProperty("com.rpkit.economy.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4390)
        saveDefaultConfig()

        messages = EconomyMessages(this)

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
                            "MYSQL" -> "com/rpkit/economy/migrations/mysql"
                            "SQLITE" -> "com/rpkit/economy/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_economy"
                ),
                classLoader
        )
        database.addTable(RPKCurrencyTable(database, this))
        database.addTable(RPKWalletTable(database, this))
        database.addTable(RPKMoneyHiddenTable(database, this))

        currencyService = RPKCurrencyServiceImpl(this)
        economyService = RPKEconomyServiceImpl(this)
        Services[RPKCurrencyService::class.java] = currencyService
        Services[RPKEconomyService::class.java] = economyService

        Services.require(RPKCharacterCardFieldService::class.java).whenAvailable { service ->
            service.characterCardFields.add(MoneyField(this))
        }

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("money")?.setExecutor(MoneyCommand(this))
        getCommand("pay")?.setExecutor(MoneyPayCommand(this))
        getCommand("wallet")?.setExecutor(MoneyWalletCommand(this))
        getCommand("currency")?.setExecutor(CurrencyCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                InventoryClickListener(this),
                InventoryCloseListener(),
                PlayerInteractListener(this),
                SignChangeListener(this)
        )
    }

}