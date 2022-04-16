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

package com.rpkit.locks.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.command.GetKeyCommand
import com.rpkit.locks.bukkit.command.KeyringCommand
import com.rpkit.locks.bukkit.command.UnlockCommand
import com.rpkit.locks.bukkit.database.table.RPKKeyringTable
import com.rpkit.locks.bukkit.database.table.RPKLockedBlockTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerGettingKeyTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerUnclaimingTable
import com.rpkit.locks.bukkit.keyring.RPKKeyringService
import com.rpkit.locks.bukkit.keyring.RPKKeyringServiceImpl
import com.rpkit.locks.bukkit.listener.*
import com.rpkit.locks.bukkit.lock.RPKLockService
import com.rpkit.locks.bukkit.lock.RPKLockServiceImpl
import com.rpkit.locks.bukkit.messages.LocksMessages
import org.bstats.bukkit.Metrics
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_INGOT
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ShapedRecipe
import java.io.File

class RPKLocksBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: LocksMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.locks.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.locks.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4402)
        saveDefaultConfig()

        messages = LocksMessages(this)

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
                            "MYSQL" -> "com/rpkit/locks/migrations/mysql"
                            "SQLITE" -> "com/rpkit/locks/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_locks"
                ),
                classLoader
        )
        database.addTable(RPKKeyringTable(database, this))
        database.addTable(RPKLockedBlockTable(database, this))
        database.addTable(RPKPlayerGettingKeyTable(database, this))
        database.addTable(RPKPlayerUnclaimingTable(database, this))

        val lockService = RPKLockServiceImpl(this)
        lockService.loadData()
        Services[RPKLockService::class.java] = lockService
        Services[RPKKeyringService::class.java] = RPKKeyringServiceImpl(this)
        val lockRecipe = ShapedRecipe(NamespacedKey(this, "lock"), lockService.lockItem)
        lockRecipe.shape("I", "B").setIngredient('I', IRON_INGOT).setIngredient('B', IRON_BLOCK)
        server.addRecipe(lockRecipe)

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("getkey")?.setExecutor(GetKeyCommand(this))
        getCommand("keyring")?.setExecutor(KeyringCommand(this))
        getCommand("unlock")?.setExecutor(UnlockCommand(this))
    }

    fun registerListeners() {
        registerListeners(
            CraftItemListener(this),
            InventoryClickListener(this),
            InventoryCloseListener(this),
            PlayerInteractListener(this),
            AsyncPlayerPreLoginListener(),
            PlayerQuitListener(),
            RPKCharacterSwitchListener()
        )
    }

}