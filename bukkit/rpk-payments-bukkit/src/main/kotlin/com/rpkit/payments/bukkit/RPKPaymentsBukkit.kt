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

package com.rpkit.payments.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.command.payment.PaymentCommand
import com.rpkit.payments.bukkit.database.table.*
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.payments.bukkit.group.RPKPaymentGroupServiceImpl
import com.rpkit.payments.bukkit.listener.PlayerJoinListener
import com.rpkit.payments.bukkit.messages.PaymentsMessages
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationService
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * RPK payments plugin default implementation.
 */
class RPKPaymentsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: PaymentsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.payments.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4406)
        saveDefaultConfig()

        messages = PaymentsMessages(this)

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
                            "MYSQL" -> "com/rpkit/payments/migrations/mysql"
                            "SQLITE" -> "com/rpkit/payments/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_payments"
                ),
                classLoader
        )
        database.addTable(RPKPaymentGroupTable(database, this))
        database.addTable(RPKPaymentGroupInviteTable(database))
        database.addTable(RPKPaymentGroupMemberTable(database))
        database.addTable(RPKPaymentGroupOwnerTable(database))
        database.addTable(RPKPaymentNotificationTable(database, this))

        Services[RPKPaymentGroupService::class.java] = RPKPaymentGroupServiceImpl(this)
        Services[RPKPaymentNotificationService::class.java] = RPKPaymentNotificationServiceImpl(this)

        // Keep payments accurate to 1 minute (60 seconds * 20 ticks)
        PaymentTask(this)
                .runTaskTimer(this, 1200L, 1200L)

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("payment")?.setExecutor(PaymentCommand(this))
    }

    fun registerListeners() {
        registerListeners(PlayerJoinListener(this))
    }

}
