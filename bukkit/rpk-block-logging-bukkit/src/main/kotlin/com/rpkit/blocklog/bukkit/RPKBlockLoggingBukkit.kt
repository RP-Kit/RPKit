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

package com.rpkit.blocklog.bukkit

import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryServiceImpl
import com.rpkit.blocklog.bukkit.command.HistoryCommand
import com.rpkit.blocklog.bukkit.command.InventoryHistoryCommand
import com.rpkit.blocklog.bukkit.command.RollbackCommand
import com.rpkit.blocklog.bukkit.database.table.RPKBlockChangeTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockHistoryTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockInventoryChangeTable
import com.rpkit.blocklog.bukkit.listener.BlockBreakListener
import com.rpkit.blocklog.bukkit.listener.BlockBurnListener
import com.rpkit.blocklog.bukkit.listener.BlockFormListener
import com.rpkit.blocklog.bukkit.listener.BlockFromToListener
import com.rpkit.blocklog.bukkit.listener.BlockIgniteListener
import com.rpkit.blocklog.bukkit.listener.BlockPistonExtendListener
import com.rpkit.blocklog.bukkit.listener.BlockPistonRetractListener
import com.rpkit.blocklog.bukkit.listener.BlockPlaceListener
import com.rpkit.blocklog.bukkit.listener.BlockSpreadListener
import com.rpkit.blocklog.bukkit.listener.EntityBlockFormListener
import com.rpkit.blocklog.bukkit.listener.EntityChangeBlockListener
import com.rpkit.blocklog.bukkit.listener.EntityExplodeListener
import com.rpkit.blocklog.bukkit.listener.InventoryClickListener
import com.rpkit.blocklog.bukkit.listener.InventoryDragListener
import com.rpkit.blocklog.bukkit.messages.BlockLoggingMessages
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKBlockLoggingBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: BlockLoggingMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.blocklog.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4380)
        saveDefaultConfig()

        messages = BlockLoggingMessages(this)
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
                            "MYSQL" -> "com/rpkit/blocklog/migrations/mysql"
                            "SQLITE" -> "com/rpkit/blocklog/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_blocklogging"
                ),
                classLoader
        )
        database.addTable(RPKBlockChangeTable(database, this))
        database.addTable(RPKBlockHistoryTable(database, this))
        database.addTable(RPKBlockInventoryChangeTable(database, this))

        Services[RPKBlockHistoryService::class.java] = RPKBlockHistoryServiceImpl(this)

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("history")?.setExecutor(HistoryCommand(this))
        getCommand("inventoryhistory")?.setExecutor(InventoryHistoryCommand(this))
        getCommand("rollback")?.setExecutor(RollbackCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                BlockBreakListener(this),
                BlockBurnListener(this),
                BlockFormListener(this),
                BlockFromToListener(this),
                BlockIgniteListener(this),
                BlockPistonExtendListener(this),
                BlockPistonRetractListener(),
                BlockPlaceListener(this),
                BlockSpreadListener(this),
                EntityBlockFormListener(this),
                EntityChangeBlockListener(),
                EntityExplodeListener(this),
                InventoryClickListener(this),
                InventoryDragListener(this)
        )
    }

}