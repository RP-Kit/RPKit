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

package com.rpkit.essentials.bukkit

import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteService
import com.rpkit.essentials.bukkit.command.*
import com.rpkit.essentials.bukkit.dailyquote.RPKDailyQuoteServiceImpl
import com.rpkit.essentials.bukkit.database.table.RPKLogMessagesEnabledTable
import com.rpkit.essentials.bukkit.database.table.RPKPreviousLocationTable
import com.rpkit.essentials.bukkit.database.table.RPKTrackingDisabledTable
import com.rpkit.essentials.bukkit.kit.RPKKitImpl
import com.rpkit.essentials.bukkit.kit.RPKKitServiceImpl
import com.rpkit.essentials.bukkit.listener.PlayerJoinListener
import com.rpkit.essentials.bukkit.listener.PlayerQuitListener
import com.rpkit.essentials.bukkit.listener.PlayerTeleportListener
import com.rpkit.essentials.bukkit.locationhistory.RPKLocationHistoryServiceImpl
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageService
import com.rpkit.essentials.bukkit.messages.EssentialsMessages
import com.rpkit.essentials.bukkit.time.TimeRunnable
import com.rpkit.essentials.bukkit.tracking.RPKTrackingServiceImpl
import com.rpkit.kit.bukkit.kit.RPKKitService
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryService
import com.rpkit.tracking.bukkit.tracking.RPKTrackingService
import org.bstats.bukkit.Metrics
import org.bukkit.GameRule.DO_DAYLIGHT_CYCLE
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import java.io.File
import java.time.Duration
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class RPKEssentialsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: EssentialsMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.essentials.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.essentials.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4392)
        ConfigurationSerialization.registerClass(RPKKitImpl::class.java)
        saveDefaultConfig()

        messages = EssentialsMessages(this)

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
                            "MYSQL" -> "com/rpkit/essentials/migrations/mysql"
                            "SQLITE" -> "com/rpkit/essentials/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_essentials"
                ),
                classLoader
        )
        database.addTable(RPKLogMessagesEnabledTable(database, this))
        database.addTable(RPKPreviousLocationTable(database, this))
        database.addTable(RPKTrackingDisabledTable(database, this))

        Services[RPKDailyQuoteService::class.java] = RPKDailyQuoteServiceImpl(this)
        Services[RPKKitService::class.java] = RPKKitServiceImpl(this)
        Services[RPKLocationHistoryService::class.java] = RPKLocationHistoryServiceImpl(this)
        Services[RPKLogMessageService::class.java] = RPKLogMessageService(this)
        Services[RPKTrackingService::class.java] = RPKTrackingServiceImpl(this)

        // Config migration from pre-2.2 time change settings
        if (config.contains("time-slow-factor")) {
            config.set("time.change-speed-enabled", true)
            config.set("time.day-duration", Duration.ofSeconds((600 * config.getDouble("time-slow-factor")).roundToLong()).toString())
            config.set("time.night-duration", Duration.ofSeconds((600 * config.getDouble("time-slow-factor")).roundToLong()).toString())
            config.set("time-slow-factor", null)
            saveConfig()
        }

        if (config.getBoolean("time.change-speed-enabled")) {
            server.worlds.forEach { world ->
                world.setGameRule(DO_DAYLIGHT_CYCLE, false)
            }
            val dayDuration = Duration.parse(config.getString("time.day-duration"))
            val nightDuration = Duration.parse(config.getString("time.night-duration"))
            val maxDuration = max(dayDuration.toSeconds(), nightDuration.toSeconds())
            val tickInterval = if (maxDuration > 600) {
                (maxDuration.toDouble() / 600.0).roundToInt() * 20
            } else {
                100
            }
            TimeRunnable(
                this,
                dayDuration,
                nightDuration,
                tickInterval
            ).runTaskTimer(this, tickInterval.toLong(), tickInterval.toLong())
        } else {
            server.worlds.forEach { world ->
                world.setGameRule(DO_DAYLIGHT_CYCLE, true)
            }
        }

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("back")?.setExecutor(BackCommand(this))
        getCommand("clone")?.setExecutor(CloneCommand(this))
        getCommand("distance")?.setExecutor(DistanceCommand(this))
        getCommand("enchant")?.setExecutor(EnchantCommand(this))
        getCommand("feed")?.setExecutor(FeedCommand(this))
        getCommand("fly")?.setExecutor(FlyCommand(this))
        getCommand("getbook")?.setExecutor(GetBookCommand(this))
        getCommand("getsign")?.setExecutor(GetSignCommand(this))
        getCommand("heal")?.setExecutor(HealCommand(this))
        getCommand("inventory")?.setExecutor(InventoryCommand(this))
        getCommand("item")?.setExecutor(ItemCommand(this))
        getCommand("itemmeta")?.setExecutor(ItemMetaCommand(this))
        getCommand("jump")?.setExecutor(JumpCommand(this))
        getCommand("kit")?.setExecutor(KitCommand(this))
        getCommand("repair")?.setExecutor(RepairCommand(this))
        getCommand("runas")?.setExecutor(RunAsCommand(this))
        getCommand("saveitem")?.setExecutor(SaveItemCommand(this).toBukkit())
        getCommand("showitem")?.setExecutor(ShowItemCommand(this))
        getCommand("seen")?.setExecutor(SeenCommand(this))
        getCommand("setspawn")?.setExecutor(SetSpawnCommand(this))
        getCommand("smite")?.setExecutor(SmiteCommand(this))
        getCommand("spawn")?.setExecutor(SpawnCommand(this))
        getCommand("spawner")?.setExecutor(SpawnerCommand(this))
        getCommand("spawnmob")?.setExecutor(SpawnMobCommand(this))
        getCommand("speed")?.setExecutor(SpeedCommand(this))
        getCommand("sudo")?.setExecutor(SudoCommand(this))
        getCommand("togglelogmessages")?.setExecutor(ToggleLogMessagesCommand(this))
        getCommand("toggletracking")?.setExecutor(ToggleTrackingCommand(this))
        getCommand("track")?.setExecutor(TrackCommand(this))
        getCommand("unsign")?.setExecutor(UnsignCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                PlayerJoinListener(this),
                PlayerQuitListener(this),
                PlayerTeleportListener()
        )
    }

}