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

package com.rpkit.store.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.store.bukkit.command.ClaimCommand
import com.rpkit.store.bukkit.command.PurchaseCommand
import com.rpkit.store.bukkit.command.PurchasesCommand
import com.rpkit.store.bukkit.database.table.RPKConsumablePurchaseTable
import com.rpkit.store.bukkit.database.table.RPKConsumableStoreItemTable
import com.rpkit.store.bukkit.database.table.RPKPermanentPurchaseTable
import com.rpkit.store.bukkit.database.table.RPKPermanentStoreItemTable
import com.rpkit.store.bukkit.database.table.RPKPurchaseTable
import com.rpkit.store.bukkit.database.table.RPKStoreItemTable
import com.rpkit.store.bukkit.database.table.RPKTimedPurchaseTable
import com.rpkit.store.bukkit.database.table.RPKTimedStoreItemTable
import com.rpkit.store.bukkit.messages.StoresMessages
import com.rpkit.store.bukkit.purchase.RPKPurchaseService
import com.rpkit.store.bukkit.purchase.RPKPurchaseServiceImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService
import com.rpkit.store.bukkit.storeitem.RPKStoreItemServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKStoresBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: StoresMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.store.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4421)
        saveDefaultConfig()

        messages = StoresMessages(this)
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
                            "MYSQL" -> "com/rpkit/store/migrations/mysql"
                            "SQLITE" -> "com/rpkit/store/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_stores"
                ),
                classLoader
        )
        database.addTable(RPKPurchaseTable(database, this))
        database.addTable(RPKConsumablePurchaseTable(database, this))
        database.addTable(RPKPermanentPurchaseTable(database, this))
        database.addTable(RPKTimedPurchaseTable(database, this))
        database.addTable(RPKStoreItemTable(database, this))
        database.addTable(RPKConsumableStoreItemTable(database, this))
        database.addTable(RPKPermanentStoreItemTable(database, this))
        database.addTable(RPKTimedStoreItemTable(database, this))

        Services[RPKPurchaseService::class.java] = RPKPurchaseServiceImpl(this)
        Services[RPKStoreItemService::class.java] = RPKStoreItemServiceImpl(this)

        registerCommands()
    }

    fun registerCommands() {
        getCommand("purchase")?.setExecutor(PurchaseCommand(this))
        getCommand("purchases")?.setExecutor(PurchasesCommand(this))
        getCommand("claim")?.setExecutor(ClaimCommand(this))
    }

}