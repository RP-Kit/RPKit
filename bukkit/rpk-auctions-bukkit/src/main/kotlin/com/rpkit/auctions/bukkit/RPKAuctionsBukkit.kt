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

package com.rpkit.auctions.bukkit

import com.rpkit.auctions.bukkit.auction.RPKAuctionService
import com.rpkit.auctions.bukkit.auction.RPKAuctionServiceImpl
import com.rpkit.auctions.bukkit.bid.RPKBidService
import com.rpkit.auctions.bukkit.bid.RPKBidServiceImpl
import com.rpkit.auctions.bukkit.command.auction.AuctionCommand
import com.rpkit.auctions.bukkit.command.bid.BidCommand
import com.rpkit.auctions.bukkit.database.table.RPKAuctionTable
import com.rpkit.auctions.bukkit.database.table.RPKBidTable
import com.rpkit.auctions.bukkit.listener.PlayerInteractListener
import com.rpkit.auctions.bukkit.listener.SignChangeListener
import com.rpkit.auctions.bukkit.messages.AuctionsMessages
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
 * RPK auctions plugin default implementation.
 */
class RPKAuctionsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: AuctionsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.auctions.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.auctions.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4376)
        saveDefaultConfig()

        messages = AuctionsMessages(this)
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
                            "MYSQL" -> "com/rpkit/auctions/migrations/mysql"
                            "SQLITE" -> "com/rpkit/auctions/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_auctions"
                ),
                classLoader
        )
        database.addTable(RPKAuctionTable(database, this))
        database.addTable(RPKBidTable(database, this))

        Services[RPKAuctionService::class.java] = RPKAuctionServiceImpl(this)
        Services[RPKBidService::class.java] = RPKBidServiceImpl(this)

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("auction")?.setExecutor(AuctionCommand(this))
        getCommand("bid")?.setExecutor(BidCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                SignChangeListener(this),
                PlayerInteractListener(this)
        )
    }

}