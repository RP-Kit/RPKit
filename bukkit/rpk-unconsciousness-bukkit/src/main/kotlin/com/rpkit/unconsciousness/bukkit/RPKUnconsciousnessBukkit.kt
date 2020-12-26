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

package com.rpkit.unconsciousness.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.unconsciousness.bukkit.command.WakeCommand
import com.rpkit.unconsciousness.bukkit.database.table.RPKUnconsciousStateTable
import com.rpkit.unconsciousness.bukkit.listener.EntityDamageByEntityListener
import com.rpkit.unconsciousness.bukkit.listener.EntityDamageListener
import com.rpkit.unconsciousness.bukkit.listener.EntityTargetListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerCommandPreprocessListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerDeathListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerInteractEntityListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerInteractListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerJoinListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerMoveListener
import com.rpkit.unconsciousness.bukkit.listener.PlayerRespawnListener
import com.rpkit.unconsciousness.bukkit.messages.UnconsciousnessMessages
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.GameRule.KEEP_INVENTORY
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class RPKUnconsciousnessBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: UnconsciousnessMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.unconsciousness.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4425)
        saveDefaultConfig()

        messages = UnconsciousnessMessages(this)

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
                            "MYSQL" -> "com/rpkit/unconsciousness/migrations/mysql"
                            "SQLITE" -> "com/rpkit/unconsciousness/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_unconsciousness"
                ),
                classLoader
        )
        database.addTable(RPKUnconsciousStateTable(database, this))

        Services[RPKUnconsciousnessService::class.java] = RPKUnconsciousnessServiceImpl(this)

        WakeTask(this).runTaskTimer(this, 200L, 200L)
        server.worlds.forEach { world ->
            world.setGameRule(KEEP_INVENTORY, true)
        }

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("wake")?.setExecutor(WakeCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                PlayerDeathListener(),
                PlayerRespawnListener(this),
                PlayerMoveListener(),
                PlayerInteractEntityListener(this),
                PlayerCommandPreprocessListener(this),
                EntityDamageListener(this),
                EntityTargetListener(this),
                PlayerJoinListener(),
                PlayerInteractListener(),
                EntityDamageByEntityListener(this)
        )
    }

}
