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

    override fun onEnable() {
        Metrics(this, 4376)
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

        Services[RPKAuctionService::class] = RPKAuctionServiceImpl(this)
        Services[RPKBidService::class] = RPKBidServiceImpl(this)
    }

    override fun registerCommands() {
        getCommand("auction")?.setExecutor(AuctionCommand(this))
        getCommand("bid")?.setExecutor(BidCommand(this))
    }

    override fun registerListeners() {
        registerListeners(
                SignChangeListener(this),
                PlayerInteractListener(this)
        )
    }

    override fun setDefaultMessages() {
        messages.setDefault("auction-usage", "&cUsage: /auction [create]")
        messages.setDefault("auction-set-currency-prompt", "&fWhich currency would you like to use? &7(Type cancel to cancel)")
        messages.setDefault("auction-set-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("auction-set-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("auction-set-currency-valid", "&aCurrency set.")
        messages.setDefault("auction-set-duration-prompt", "&fHow long would you like the auction to be (in hours)?")
        messages.setDefault("auction-set-duration-invalid-number", "&cYou must specify a number for the duration.")
        messages.setDefault("auction-set-duration-invalid-negative", "&cYou may not set the duration to a negative number.")
        messages.setDefault("auction-set-duration-valid", "&aDuration set.")
        messages.setDefault("auction-set-start-price-prompt", "&fWhat would you like to set the start price to?")
        messages.setDefault("auction-set-start-price-invalid-number", "&cYou must specify a number for the start price.")
        messages.setDefault("auction-set-start-price-invalid-negative", "&cYou may not set the start price to a negative number.")
        messages.setDefault("auction-set-start-price-valid", "&aStart price set.")
        messages.setDefault("auction-set-buy-out-price-prompt", "&fWhat would you like to set the buy out price to?")
        messages.setDefault("auction-set-buy-out-price-invalid-number", "&cYou must specify a number for the buy out price.")
        messages.setDefault("auction-set-buy-out-price-invalid-negative", "&cYou may not set the buy out price to a negative number.")
        messages.setDefault("auction-set-buy-out-price-valid", "&aBuy out price set.")
        messages.setDefault("auction-set-no-sell-price-prompt", "&fWhat would you like to set the no sell price to?")
        messages.setDefault("auction-set-no-sell-price-invalid-number", "&cYou must specify a number for the no sell price.")
        messages.setDefault("auction-set-no-sell-price-invalid-negative", "&cYou may not set the no sell price to a negative number.")
        messages.setDefault("auction-set-no-sell-price-valid", "&aNo sell price set.")
        messages.setDefault("auction-set-minimum-bid-increment-prompt", "&fWhat would you like to set the minimum bid increment to?")
        messages.setDefault("auction-set-minimum-bid-increment-invalid-number", "&cYou must specify a number for the minimum bid increment.")
        messages.setDefault("auction-set-minimum-bid-increment-invalid-negative", "&cYou may not set the minimum bid increment to a negative number.")
        messages.setDefault("auction-set-minimum-bid-increment-valid", "&aMinimum bid increment set.")
        messages.setDefault("auction-create-valid", "&aAuction created.")
        messages.setDefault("auction-create-id", "&aYour auction ID is \$id. Please inform bidders to use /bid \$id [amount]. You may also create an auction sign with [auction] on the first line and \$id on the second line.")
        messages.setDefault("auction-item-received", "&aReceived \$amount \$item from \$character's auction (auction ID \$auction_id).")
        messages.setDefault("auction-item-returned", "&aYour \$item \$amount was returned due to your auction failing (auction ID \$auction_id).")
        messages.setDefault("bid-usage", "&cUsage: /bid [auction ID] [amount]")
        messages.setDefault("bid-invalid-not-high-enough", "&cYou must bid at least \$amount.")
        messages.setDefault("bid-invalid-not-enough-money", "&cYou do not have enough money to bid that high.")
        messages.setDefault("bid-invalid-auction-not-open", "&cThat auction is not currently open.")
        messages.setDefault("bid-invalid-auction-not-existent", "&cThat auction does not exist.")
        messages.setDefault("bid-invalid-amount-not-a-number", "&cYou must specify a number for the amount to bid.")
        messages.setDefault("bid-invalid-auction-id-not-a-number", "&cYou must specify a number for the auction ID.")
        messages.setDefault("bid-invalid-too-far-away", "&cYou are not close enough to that auction.")
        messages.setDefault("bid-valid", "&aBid \$amount \$currency for \$item.")
        messages.setDefault("bid-created", "&a\$character bid \$amount \$currency on auction \$auction_id for \$item.")
        messages.setDefault("auction-sign-invalid-id-not-a-number", "&cPlease write the auction ID on the second line.")
        messages.setDefault("auction-sign-invalid-auction-does-not-exist", "&cThere is no auction with that ID.")
        messages.setDefault("no-character", "&cYou need a character to perform this action.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("operation-cancelled", "&cOperation cancelled.")
        messages.setDefault("no-permission-bid", "&cYou do not have permission to bid.")
        messages.setDefault("no-permission-auction-create", "&cYou do not have permission to create auctions.")
        messages.setDefault("auction-create-failed", "&cFailed to create auction.")
        messages.setDefault("auction-update-failed", "&cFailed to update auction.")
        messages.setDefault("auction-delete-failed", "&cFailed to delete auction.")
        messages.setDefault("bid-create-failed", "&cFailed to create bid.")
        messages.setDefault("bid-update-failed", "&cFailed to update bid.")
        messages.setDefault("bid-delete-failed", "&cFailed to delete bid.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-currency-service", "&cThere is no currency service available.")
        messages.setDefault("no-economy-service", "&cThere is no economy service available.")
        messages.setDefault("no-auction-service", "&cThere is no auction service available.")
    }

}