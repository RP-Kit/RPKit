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

package com.rpkit.essentials.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteService
import com.rpkit.essentials.bukkit.command.BackCommand
import com.rpkit.essentials.bukkit.command.CloneCommand
import com.rpkit.essentials.bukkit.command.DistanceCommand
import com.rpkit.essentials.bukkit.command.EnchantCommand
import com.rpkit.essentials.bukkit.command.FeedCommand
import com.rpkit.essentials.bukkit.command.FlyCommand
import com.rpkit.essentials.bukkit.command.GetBookCommand
import com.rpkit.essentials.bukkit.command.GetSignCommand
import com.rpkit.essentials.bukkit.command.HealCommand
import com.rpkit.essentials.bukkit.command.InventoryCommand
import com.rpkit.essentials.bukkit.command.ItemCommand
import com.rpkit.essentials.bukkit.command.ItemMetaCommand
import com.rpkit.essentials.bukkit.command.JumpCommand
import com.rpkit.essentials.bukkit.command.KitCommand
import com.rpkit.essentials.bukkit.command.RepairCommand
import com.rpkit.essentials.bukkit.command.RunAsCommand
import com.rpkit.essentials.bukkit.command.SeenCommand
import com.rpkit.essentials.bukkit.command.SetSpawnCommand
import com.rpkit.essentials.bukkit.command.SmiteCommand
import com.rpkit.essentials.bukkit.command.SpawnCommand
import com.rpkit.essentials.bukkit.command.SpawnMobCommand
import com.rpkit.essentials.bukkit.command.SpawnerCommand
import com.rpkit.essentials.bukkit.command.SpeedCommand
import com.rpkit.essentials.bukkit.command.SudoCommand
import com.rpkit.essentials.bukkit.command.ToggleLogMessagesCommand
import com.rpkit.essentials.bukkit.command.ToggleTrackingCommand
import com.rpkit.essentials.bukkit.command.TrackCommand
import com.rpkit.essentials.bukkit.command.UnsignCommand
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
import com.rpkit.essentials.bukkit.time.TimeSlowRunnable
import com.rpkit.essentials.bukkit.tracking.RPKTrackingServiceImpl
import com.rpkit.kit.bukkit.kit.RPKKitService
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryService
import com.rpkit.tracking.bukkit.tracking.RPKTrackingService
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import java.io.File


class RPKEssentialsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        Metrics(this, 4392)
        ConfigurationSerialization.registerClass(RPKKitImpl::class.java)
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
                            "MYSQL" -> "com/rpkit/essentials/migrations/mysql"
                            "SQLITE" -> "com/rpkit/essentials/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_players"
                ),
                classLoader
        )
        database.addTable(RPKLogMessagesEnabledTable(database, this))
        database.addTable(RPKPreviousLocationTable(database, this))
        database.addTable(RPKTrackingDisabledTable(database, this))

        Services[RPKDailyQuoteService::class] = RPKDailyQuoteServiceImpl(this)
        Services[RPKKitService::class] = RPKKitServiceImpl(this)
        Services[RPKLocationHistoryService::class] = RPKLocationHistoryServiceImpl(this)
        Services[RPKLogMessageService::class] = RPKLogMessageService(this)
        Services[RPKTrackingService::class] = RPKTrackingServiceImpl(this)

        TimeSlowRunnable(this).runTaskTimer(this, 100L, 100L)
    }

    override fun registerCommands() {
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

    override fun registerListeners() {
        registerListeners(
                PlayerJoinListener(this),
                PlayerQuitListener(this),
                PlayerTeleportListener()
        )
    }

    override fun setDefaultMessages() {
        messages.setDefault("back-valid", "&aTeleported to previous location.")
        messages.setDefault("back-invalid-no-locations", "&cYou have not teleported recently.")
        messages.setDefault("clone-valid", "&aItem cloned.")
        messages.setDefault("clone-invalid-item", "&cYou are not holding any item.")
        messages.setDefault("distance-invalid-untrackable", "&cThat player is not currently trackable.")
        messages.setDefault("distance-invalid-item", "&cYou require \$amount x \$type to use that command.")
        messages.setDefault("distance-invalid-distance", "&cYou are too far away to get the distance to that player.")
        messages.setDefault("distance-valid", "&aDistance to \$character/\$player: \$distance")
        messages.setDefault("distance-untrackable-notification", "&c\$player attempted to check their distance to you. If you wish for them to be able to find you, re-enable tracking with /toggletracking.")
        messages.setDefault("distance-invalid-world", "&cThat player is in a different world.")
        messages.setDefault("distance-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("distance-usage", "&cUsage: /distance [player]")
        messages.setDefault("enchant-valid", "&aSuccessfully enchanted \$amount x \$type with \$enchantment \$level")
        messages.setDefault("enchant-invalid-level", "&cThe enchantment level must be a number.")
        messages.setDefault("enchant-invalid-enchantment", "&cThere is no enchantment by that name.")
        messages.setDefault("enchant-invalid-illegal", "&cThat enchantment is illegal.")
        messages.setDefault("enchant-invalid-item", "&cYou must be holding something in order to enchant it.")
        messages.setDefault("enchant-usage", "&cUsage: /enchant [enchantment] [level]")
        messages.setDefault("feed-notification", "&aHunger refilled.")
        messages.setDefault("feed-valid", "&a\$player's hunger was refilled.")
        messages.setDefault("feed-usage-console", "&cWhen using this command from console, you must specify a player to feed.")
        messages.setDefault("fly-enable-notification", "&aFly mode enabled.")
        messages.setDefault("fly-enable-valid", "&aAllowed \$player to fly.")
        messages.setDefault("fly-disable-notification", "&aFly mode disabled.")
        messages.setDefault("fly-disable-valid", "&aDisallowed \$player to fly.")
        messages.setDefault("fly-usage-console", "&cWhen using this command from console, you must specify a player.")
        messages.setDefault("get-book-valid", "&aHere's a book.")
        messages.setDefault("get-sign-valid", "&aHere's a sign.")
        messages.setDefault("heal-notification", "&aHealed.")
        messages.setDefault("heal-valid", "&aHealed \$player.")
        messages.setDefault("heal-usage-console", "&cWhen using this command from console, you must specify a player to heal.")
        messages.setDefault("inventory-valid", "&aViewing \$player's inventory.")
        messages.setDefault("inventory-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("inventory-usage", "&cUsage: /inventory [player]")
        messages.setDefault("item-invalid-amount", "&cThe amount must be an integer.")
        messages.setDefault("item-valid-plural", "&aCreated \$amount \$types.")
        messages.setDefault("item-valid-singular", "&aCreated a \$type.")
        messages.setDefault("item-invalid-material", "&cCould not find a material by that name.")
        messages.setDefault("item-usage", "&cUsage: /item [type] [amount]")
        messages.setDefault("item-meta-set-name-valid", "&aSet item display name to \$name.")
        messages.setDefault("item-meta-add-lore-valid", "&aAdded lore: \"\$lore&a\".")
        messages.setDefault("item-meta-remove-lore-valid", "&cRemoved lore: \"\$lore&a\".")
        messages.setDefault("item-meta-remove-lore-invalid-lore-item", "&cThat item does not have that piece of lore.")
        messages.setDefault("item-meta-remove-lore-invalid-lore", "&cThat item does not have any lore.")
        messages.setDefault("item-meta-usage", "&cUsage: /itemmeta [setname|addlore|removelore] [name|lore]")
        messages.setDefault("item-meta-invalid-item", "&cYou must be holding an item to modify its item meta.")
        messages.setDefault("jump-valid", "&aTeleported to first block in line of sight.")
        messages.setDefault("jump-invalid-block", "&cNo block was found within 64 blocks of your line of sight.")
        messages.setDefault("kit-valid", "&aClaimed \$kit.")
        messages.setDefault("kit-invalid-kit", "&cThere is no kit by that name.")
        messages.setDefault("kit-list-title", "&fKit list:")
        messages.setDefault("kit-list-item", "&f- &7\$kit")
        messages.setDefault("repair-valid", "&aItem repaired.")
        messages.setDefault("repair-invalid-item", "&cYou must be holding an item in order to repair it.")
        messages.setDefault("run-as-valid", "&aDispatched command.")
        messages.setDefault("run-as-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("run-as-usage", "&cUsage: /runas [player] [command]")
        messages.setDefault("seen-online", "&a\$player is online now.")
        messages.setDefault("seen-date", "&a\$player was last seen on \$date at \$time.")
        messages.setDefault("seen-diff", "&a(That's \$days days, \$hours hours, \$minutes minutes, \$seconds seconds ago.)")
        messages.setDefault("seen-never", "&aThat player has never played on this server.")
        messages.setDefault("seen-usage", "&cUsage: /seen [player]")
        messages.setDefault("set-spawn-valid", "&aSpawn location of \$world set.")
        messages.setDefault("smite-usage", "&cUsage: /smite [player]")
        messages.setDefault("smite-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("smite-valid", "&aOpening the heavens on \$player.")
        messages.setDefault("spawn-valid", "&aTeleported to spawn.")
        messages.setDefault("spawner-valid", "&aSpawner type set.")
        messages.setDefault("spawner-invalid-entity", "&cThere is no entity by that name.")
        messages.setDefault("spawner-invalid-block", "&cThat's not a mob spawner.")
        messages.setDefault("spawner-usage", "&cUsage: /spawner [type]")
        messages.setDefault("spawn-mob-valid", "&aMobs spawned.")
        messages.setDefault("spawn-mob-invalid-amount", "&cThe amount of mobs must be an integer.")
        messages.setDefault("spawn-mob-invalid-mob", "&cThat entity type does not exist.")
        messages.setDefault("spawn-mob-usage", "&cUsage: /spawnmob [type] [amount]")
        messages.setDefault("speed-invalid-speed-number", "&cSpeed must be a number.")
        messages.setDefault("speed-reset-valid", "&aReset \$player's fly speed.")
        messages.setDefault("speed-reset-notification", "&aYour fly speed was reset by \$player.")
        messages.setDefault("speed-set-valid", "&aSet \$player's fly speed to \$speed.")
        messages.setDefault("speed-set-notification", "&aYour fly speed was set to \$speed by \$player.")
        messages.setDefault("speed-invalid-speed-bounds", "&cSpeed must be between -1.0 and 1.0")
        messages.setDefault("speed-usage-console", "&cWhen using this command from console, you must specify a player to change the fly speed of.")
        messages.setDefault("toggle-log-messages-valid", "&aLog messages \$enabled.")
        messages.setDefault("toggle-tracking-on-valid", "&aYou have allowed people to find you with /track and /distance.")
        messages.setDefault("toggle-tracking-off-valid", "&aYou have disallowed people to find you with /track and /distance.")
        messages.setDefault("track-invalid-untrackable", "&cThat player is not currently trackable.")
        messages.setDefault("track-invalid-item", "&cYou require \$amount x \$type to use that command.")
        messages.setDefault("track-invalid-distance", "&cYou are too far away to track that player.")
        messages.setDefault("track-valid", "&aNow tracking \$player/\$character.")
        messages.setDefault("track-untrackable-notification", "&c\$player attempted to track you. If you wish for them to be able to find you, re-enable tracking with /toggletracking.")
        messages.setDefault("track-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("track-usage", "&cUsage: /track [player]")
        messages.setDefault("unsign-valid", "&aBook unsigned.")
        messages.setDefault("unsign-invalid-book", "&cYou must be holding a written book to unsign.")
        messages.setDefault("no-permission-back", "&cYou do not have permission to go back.")
        messages.setDefault("no-permission-clone", "&cYou do not have permission to clone items.")
        messages.setDefault("no-permission-distance", "&cYou do not have permission to check your distance to players.")
        messages.setDefault("no-permission-enchant", "&cYou do not have permission to enchant items.")
        messages.setDefault("no-permission-feed", "&cYou do not have permission to feed players.")
        messages.setDefault("no-permission-fly", "&cYou do not have permission to toggle fly mode.")
        messages.setDefault("no-permission-get-book", "&cYou do not have permission to get books.")
        messages.setDefault("no-permission-get-sign", "&cYou do not have permission to get signs.")
        messages.setDefault("no-permission-heal", "&cYou do not have permission to heal players.")
        messages.setDefault("no-permission-inventory", "&cYou do not have permission to view other people's inventories.")
        messages.setDefault("no-permission-item", "&cYou do not have permission to spawn items.")
        messages.setDefault("no-permission-item-meta", "&cYou do not have permission to modify item meta.")
        messages.setDefault("no-permission-jump", "&cYou do not have permission to jump.")
        messages.setDefault("no-permission-kit", "&cYou do not have permission to claim kits.")
        messages.setDefault("no-permission-repair", "&cYou do not have permission to repair items.")
        messages.setDefault("no-permission-run-as", "&cYou do not have permission to run commands as other players.")
        messages.setDefault("no-permission-seen", "&cYou do not have permission to see when players were last online.")
        messages.setDefault("no-permission-set-spawn", "&cYou do not have permission to set the spawn.")
        messages.setDefault("no-permission-smite", "&cYou do not have permission to smite people.")
        messages.setDefault("no-permission-spawn", "&cYou do not have permission to teleport to spawn.")
        messages.setDefault("no-permission-spawner", "&cYou do not have permission to change spawner types.")
        messages.setDefault("no-permission-spawn-mob", "&cYou do not have permission to spawn mobs.")
        messages.setDefault("no-permission-speed", "&cYou do not have permission to set fly speed.")
        messages.setDefault("no-permission-sudo", "&c\$player is not in the sudoers file. This incident will be reported.")
        messages.setDefault("no-permission-toggle-log-messages", "&cYou do not have permission to toggle log messages.")
        messages.setDefault("no-permission-toggle-tracking", "&cYou do not have permission to toggle tracking.")
        messages.setDefault("no-permission-track", "&cYou do not have permission to track players.")
        messages.setDefault("no-permission-unsign", "&cYou do not have permission to unsign written books.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform this command.")
        messages.setDefault("no-character-self", "&cYou must have a character to perform that command.")
        messages.setDefault("no-character-other", "&cThat player must have a character to perform that command.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-location-history-service", "&cThere is no location history service available.")
        messages.setDefault("no-kit-service", "&cThere is no kit service available.")
        messages.setDefault("no-log-message-service", "&cThere is no log message service.")
        messages.setDefault("no-tracking-service", "&cThere is no tracking service available.")
    }

}