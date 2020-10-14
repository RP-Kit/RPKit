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
import com.rpkit.store.bukkit.purchase.RPKPurchaseService
import com.rpkit.store.bukkit.purchase.RPKPurchaseServiceImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemService
import com.rpkit.store.bukkit.storeitem.RPKStoreItemServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKStoresBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        Metrics(this, 4421)
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
                            "MYSQL" -> "com/rpkit/stores/migrations/mysql"
                            "SQLITE" -> "com/rpkit/stores/migrations/sqlite"
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

        Services[RPKPurchaseService::class] = RPKPurchaseServiceImpl(this)
        Services[RPKStoreItemService::class] = RPKStoreItemServiceImpl(this)
    }

    override fun registerCommands() {
        getCommand("purchase")?.setExecutor(PurchaseCommand(this))
        getCommand("purchases")?.setExecutor(PurchasesCommand(this))
        getCommand("claim")?.setExecutor(ClaimCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("no-permission-claim", "&cYou do not have permission to claim purchases.")
        messages.setDefault("claim-usage", "&cUsage: /claim [purchase id]")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("no-minecraft-profile-self", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-profile-self", "&cA profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("claim-purchase-id-invalid-integer", "&cYou must specify a valid purchase ID.")
        messages.setDefault("claim-purchase-id-invalid-purchase", "&cYou must specify a valid purchase ID.")
        messages.setDefault("claim-purchase-id-invalid-consumable", "&cThat purchase is not consumable.")
        messages.setDefault("claim-purchase-id-invalid-profile", "&cThat purchase is not yours to claim.")
        messages.setDefault("claim-plugin-not-installed", "&cThe plugin that purchase is associated with is not currently installed. Please contact a member of staff for support.")
        messages.setDefault("claim-plugin-cannot-claim", "&cThe plugin that purchase is associated with does not support claiming purchases. Please contact a member of staff for support.")
        messages.setDefault("claim-successful", "&aSuccessfully claimed your purchase.")
        messages.setDefault("only-from-console", "&cYou may only use this command from console.")
        messages.setDefault("purchase-usage", "&cUsage: /purchase [uuid] [store item id]")
        messages.setDefault("no-minecraft-profile-other", "&c\$name/\$uuid does not have a Minecraft profile.")
        messages.setDefault("no-profile-other", "&c\$name/\$uuid does not have a profile")
        messages.setDefault("purchase-store-item-id-invalid-integer", "&cYou must specify a valid store item ID.")
        messages.setDefault("purchase-store-item-id-invalid-item", "&cYou must specify a valid store item ID.")
        messages.setDefault("purchase-successful", "&aPurchase successful.")
        messages.setDefault("no-permission-purchases", "&cYou do not have permission to view your purchases.")
        messages.setDefault("purchases-title", "&fPurchases:")
        messages.setDefault("purchases-item", "&7\$purchase_id - \$store_item_plugin:\$store_item_identifier \$store_item_description - \$purchase_date")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-purchase-service", "&cThere is no purchase service available.")
        messages.setDefault("no-store-item-service", "&cThere is no store item service available.")
    }

}